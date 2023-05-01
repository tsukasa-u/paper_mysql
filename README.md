# paper_mysql

minecraft paper server のインベントリを同期するためのプラグインです。導入にはmySQLが必要となります。

<br>

# Table of contents

* [Table of contents](#Introduction)
* [Requirements](#requirements)
  * [Linux and macOS](#linux-and-macos)
  * [Windows](#windows)
* [Command-Line Interface](#command-line-interface)

# Download

[paper_mysql.jar (2023/03/15)](https://cdn.jsdelivr.net/gh/tsukasa-u/paper_mysql@main/out/artifacts/paper_mysql_jar/paper_mysql.jar)

<br>

# Introduction

## Step1
1. サーバーの`plagins`フォルダにダウンロードした`paper-mysql.jar`を移動。
2. サーバーを起動。

```
java -jar paper-*.jar nogui
```

3. `plagins\paper_mysql\config.yml`が生成される。 

## Step2
1. mySQLを導入する。詳しくは[https://www.mysql.com/jp](https://www.mysql.com/jp/)や他サイトを確認してほしい。
```
apt install mysql-server
```
2. mySQLに接続。`user_name`は適宜置き換える(ディフォルトはroot)(以下略)。
```
mysql -u user_name -p
```
3. データベースを作成する。`database_name`は適宜置き換える(以下略)。
```
CREATE DATABASE database_name DEFAULT CHARACTER SET utf8;
```
4. データベースへ接続。
```
USE database_name;
```
5. テーブルの作成。
```
CREATE TABLE inventory (name varchar(16), uuid varchar(36) NOT NULL PRIMARY KEY, inventory_item varbinary(32768), ender_item varbinary(24576), status varchar(8) NOT NULL);
```
6. mySQLへの接続を切断。
```
\q
```

## Step3
1. Paper serverの`plagins\paper_mysql\config.yml`の設定をする。`host`の設定値は外部のサーバーで実行するmuSQLを参照したい場合はそのサーバーアドレスを設定する。ローカル環境の場合は`localhost`を設定。`port`はmySQLのポート番号を変更した場合のみ変更する。`passward`はmySQLのパスワードを設定する。
```
host: "localhost"
port: 3306
database: "database_name"
username: "user_name"
password : "passward"
```
2. ALL DONE!!

<br>

# Command

| command | discription |
|---|---|
| /paper_mysql:updateinventory | 強制的にインベントリの同期を実行する |

<br>

# test environment

| type | name | version | URL |
|---|---|---|---|
|OS | Ubuntu | 22.04 | |
|minecraft server | Paper | 1.19.3-404 | https://papermc.io/downloads/paper |
|proxy server| Velocity | 3.2.0-SNAPSHOT-226 | https://papermc.io/downloads/velocity |

<br>

# Q&A
・インベントリの同期が失敗する。