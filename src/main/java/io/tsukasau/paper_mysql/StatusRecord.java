package io.tsukasau.paper_mysql;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;


public class StatusRecord {

    private Connection connection;
    final private String host, database, username, password;
    final private int port, timeout;

    public StatusRecord(String _host, int _port, String _database, String _username, String _password, int _timeout) {
        host = _host;
        port = _port;
        database = _database;
        username = _username;
        password = _password;
        timeout = _timeout;
    }


    private byte[] save_ItemStack_JSON(Player player, int[] slot_array, String option) {


        List<byte[]> packet_length = new ArrayList<>();

        List<byte[]> serialize_tmp = new ArrayList<>();
        List<byte[]> serialize_length_tmp = new ArrayList<>();
        List<byte[]> name_tmp = new ArrayList<>();
        List<byte[]> name_length_tmp = new ArrayList<>();
        List<byte[]> amount_tmp = new ArrayList<>();

        int max = 1 + slot_array.length;
        for (int i : slot_array) {

            ItemStack item = null;
            if (Objects.equals(option, "inventory")) {
                item = player.getInventory().getItem(i);
            } else if (Objects.equals(option, "enderChest")) {
                item = player.getEnderChest().getItem(i);
            }


            byte[] bytes;
            String name;
            int amount;
            if (item == null) {

                bytes = new byte[0];
                name = "";
                amount = 0;

            } else {

                bytes = item.serializeAsBytes();
                name = item.getType().name();
                amount = item.getAmount();

            }

            int tmp_length = (bytes.length + 4 + name.length());
            max += tmp_length + 2;

            packet_length.add(new byte[]{(byte) ((tmp_length >> 8) & 0xFF), (byte) (tmp_length & 0xFF)});

            serialize_tmp.add(bytes);
            serialize_length_tmp.add(new byte[]{(byte) ((bytes.length >> 8) & 0xFF), (byte) (bytes.length & 0xFF)});
            name_tmp.add(name.getBytes());
            amount_tmp.add(new byte[]{(byte) amount});
            name_length_tmp.add(new byte[]{(byte) name.length()});
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(max);
        byteBuffer.put(new byte[]{(byte) slot_array.length});


        for (int j = 0; j < slot_array.length; ++j) {

            byteBuffer.put(packet_length.get(j));
            byteBuffer.put(name_length_tmp.get(j));
            byteBuffer.put(name_tmp.get(j));
            byteBuffer.put(serialize_length_tmp.get(j));
            byteBuffer.put(serialize_tmp.get(j));
            byteBuffer.put(amount_tmp.get(j));

        }

        return byteBuffer.array();
    }

    private void load_ItemStack_JSON(Player player, int[] slot_array, byte[] item_Stack, String option) {

        byte slot_length = item_Stack[0];
        int cnt = 1;
        for (int j = 0; j < slot_length; ++j) {

            int length_tmp = (Byte.toUnsignedInt(item_Stack[cnt]) << 8) + Byte.toUnsignedInt(item_Stack[cnt + 1]);

            cnt += 2;
            if (length_tmp == 4) {

                if (Objects.equals(option, "inventory")) {
                    player.getInventory().setItem(slot_array[j], null);
                } else if (Objects.equals(option, "enderChest")) {
                    player.getEnderChest().setItem(slot_array[j], null);
                }

            } else {

                byte[] dst = Arrays.copyOfRange(item_Stack, cnt, cnt + length_tmp);

                int cnt_tmp = 0;

                int name_length = dst[cnt_tmp];
                cnt_tmp += 1;
                String name = Arrays.copyOfRange(dst, cnt_tmp, cnt_tmp + name_length).toString();
                cnt_tmp += name_length;


                int serialize_length = (Byte.toUnsignedInt(dst[cnt_tmp]) << 8) + Byte.toUnsignedInt(dst[cnt_tmp + 1]);
                cnt_tmp += 2;
                byte[] bytes = Arrays.copyOfRange(dst, cnt_tmp, cnt_tmp + serialize_length);
                cnt_tmp += serialize_length;

                int amount = dst[cnt_tmp];

                ItemStack item = ItemStack.deserializeBytes(bytes);

                System.out.println(item);


                if (Objects.equals(option, "inventory")) {
                    player.getInventory().setItem(j, item);
                } else if (Objects.equals(option, "enderChest")) {
                    player.getEnderChest().setItem(j, item);
                }
            }

            cnt += length_tmp;
        }
    }

    public void loadPlayer(Player player, String option) {

        try {
            openConnection();
            UUID uuid = player.getUniqueId();

//            String sql = "SELECT * FROM inventory WHERE uuid='" + uuid.toString() + "';";
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            if (preparedStatement == null) return;
//
//            int steps = 0;
//
//            ResultSet rs = preparedStatement.executeQuery(sql);
//            task = Bukkit.getScheduler().runTaskTimer((Plugin) this,
            class Runnable extends BukkitRunnable {

                int steps = 0;

                @Override
                public void run() {

                    String sql = "SELECT * FROM inventory WHERE uuid='" + uuid.toString() + "';";
                    PreparedStatement preparedStatement = null;
                    try {
                        preparedStatement = connection.prepareStatement(sql);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    ResultSet rs = null;
                    try {
                        rs = preparedStatement.executeQuery(sql);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        if (rs.next()) {
                            byte[] inventory_item = rs.getBytes(3);
                            byte[] ender_item = rs.getBytes(4);
                            String sql_status = rs.getString(5);

                            if (Objects.equals(sql_status, "SAVED") || Objects.equals(option, "ENFORCE")) {

                                int[] inventory_slot_array = {
                                        0, 1, 2, 3, 4, 5, 6, 7, 8,
                                        9, 10, 11, 12, 13, 14, 15, 16, 17,
                                        18, 19, 20, 21, 22, 23, 24, 25, 26,
                                        27, 28, 29, 30, 31, 32, 33, 34, 35,
                                        36, 37, 38, 39, 40
//                                100,
//                                101,
//                                102,
//                                103,
//                                106
                                };
                                int[] ender_slot_array = {
                                        0, 1, 2, 3, 4, 5, 6, 7, 8,
                                        9, 10, 11, 12, 13, 14, 15, 16, 17,
                                        18, 19, 20, 21, 22, 23, 24, 25, 26
                                };

                                load_ItemStack_JSON(player, inventory_slot_array, inventory_item, "inventory");
                                load_ItemStack_JSON(player, ender_slot_array, ender_item, "enderChest");


                                sql = "UPDATE inventory SET status = ? WHERE uuid = ?;";
                                preparedStatement = connection.prepareStatement(sql);
                                preparedStatement.setString(1, "LOADED");
                                preparedStatement.setString(2, uuid.toString());
                                preparedStatement.executeUpdate();

                                if (Objects.equals(option, "ENFORCE")) {
                                    player.sendMessage(Component.text("enforced to update inventory!, " + player.getName() + "!"));
                                } else {
                                    player.sendMessage(Component.text("load!, " + player.getName() + "!"));
                                }

                                cancel();
//                                break;

                            } else if (Objects.equals(sql_status, "LOADED")) {

                                if (steps > timeout) {
                                    player.sendMessage(Component.text("filed to load!, " + player.getName() + "!"));
                                    cancel();
//                                    break;
                                }
                                try {
                                    Thread.sleep(200);
                                    steps += 200;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                preparedStatement = connection.prepareStatement(sql);
                                if (preparedStatement == null) return;

                                rs = preparedStatement.executeQuery(sql);
                            }
                        } else {
                            savePlayer(player, "INSERT");
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };

            new Runnable().runTaskTimer(inventory_sync.getProvidingPlugin(), 0L, 4L);
//
//            while(true) {
//                if (rs.next()) {
//                    byte[] inventory_item = rs.getBytes(3);
//                    byte[] ender_item = rs.getBytes(4);
//                    String sql_status = rs.getString(5);
//
//                    if (Objects.equals(sql_status, "SAVED") || Objects.equals(option, "ENFORCE")) {
//
//                        int[] inventory_slot_array = {
//                                0, 1, 2, 3, 4, 5, 6, 7, 8,
//                                9, 10, 11, 12, 13, 14, 15, 16, 17,
//                                18, 19, 20, 21, 22, 23, 24, 25, 26,
//                                27, 28, 29, 30, 31, 32, 33, 34, 35,
//                                36,37,38,39,40
////                                100,
////                                101,
////                                102,
////                                103,
////                                106
//                        };
//                        int[] ender_slot_array = {
//                                0, 1, 2, 3, 4, 5, 6, 7, 8,
//                                9, 10, 11, 12, 13, 14, 15, 16, 17,
//                                18, 19, 20, 21, 22, 23, 24, 25, 26
//                        };
//
//                        load_ItemStack_JSON(player, inventory_slot_array, inventory_item, "inventory");
//                        load_ItemStack_JSON(player, ender_slot_array, ender_item, "enderChest");
//
//
//                        sql = "UPDATE inventory SET status = ? WHERE uuid = ?;";
//                        preparedStatement = connection.prepareStatement(sql);
//                        preparedStatement.setString(1, "LOADED");
//                        preparedStatement.setString(2, uuid.toString());
//                        preparedStatement.executeUpdate();
//
//                        player.sendMessage(Component.text("load!, " + player.getName() + "!"));
//
//                        break;
//
//                    } else if (Objects.equals(sql_status, "LOADED")) {
//
//                        if (steps < timeout) {
//                            if (Objects.equals(option, "ENFORCE")) {
//                                player.sendMessage(Component.text("enforced to update inventory!, " + player.getName() + "!"));
//                            } else {
//                                player.sendMessage(Component.text("filed to load!, " + player.getName() + "!"));
//                            }
//                            break;
//                        }
//                        try {
//                            Thread.sleep(200);
//                            steps += 200;
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        preparedStatement = connection.prepareStatement(sql);
//                        if (preparedStatement == null) return;
//
//                        rs = preparedStatement.executeQuery(sql);
//                    }
//                } else {
//                    savePlayer(player, "INSERT");
//                }
//            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(Player player, String option) throws SQLException {
        openConnection();
        String name = player.getName();
        UUID uuid = player.getUniqueId();
        int[] inventory_slot_array = {
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26,
                27, 28, 29, 30, 31, 32, 33, 34, 35,
                36,37,38,39,40
//                100,
//                101,
//                102,
//                103,
//                106
        };
        byte[] inventory_item = save_ItemStack_JSON( player, inventory_slot_array, "inventory");
        int [] ender_slot_array = {
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15, 16, 17,
                18, 19, 20, 21, 22, 23, 24, 25, 26
        };
        byte[]  ender_item = save_ItemStack_JSON( player, ender_slot_array, "enderChest" );
//
        if (Objects.equals(option, "UPDATE")) {
            String sql = "UPDATE inventory SET inventory_item = ? , ender_item = ? , status = ? WHERE uuid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setBytes(1, inventory_item);
            preparedStatement.setBytes(2, ender_item);
            preparedStatement.setString(3, "SAVED");
            preparedStatement.setString(4, uuid.toString());
            preparedStatement.executeUpdate();
        } else if (Objects.equals(option, "INSERT")) {
            String sql = "INSERT INTO inventory (name, uuid, inventory_item, ender_item, status) VALUES (?, ?, ?, ?, ?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.setBytes(3, inventory_item);
            preparedStatement.setBytes(4, ender_item);
            preparedStatement.setString(5, "SAVED");
            preparedStatement.executeUpdate();
        }
        player.sendMessage(Component.text("save!, " + player.getName() + "!"));

    }

    public boolean searchPlayer(Player player) throws SQLException {
        openConnection();
        String name = player.getName();
        UUID uuid = player.getUniqueId();

        String sql = "SELECT * FROM inventory WHERE uuid='" + uuid.toString() +  "';";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (preparedStatement == null) return false;

        ResultSet rs = preparedStatement.executeQuery(sql);
        if (rs.next()) return true;
        return false;
    }

    public void deletePlayer(Player player) throws SQLException {
        openConnection();
        String name = player.getName();
        UUID uuid = player.getUniqueId();

        String sql = "DELETE FROM inventory WHERE uuid = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, uuid.toString());

        preparedStatement.executeUpdate();
    }

    private void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }
}


