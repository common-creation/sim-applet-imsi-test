# sim-applet-imsi-test

## 概要

IMSIを書き換えることで、SIM Applet単独で回線を遮断するデモ用リポジトリです。  
[NTTコミュニケーションズ株式会社](https://www.ntt.com/)より提供されている、[IoT Connect Mobile Type S](https://sdpf.ntt.com/services/icms/)のeSIMにインストールされるのを想定していますが、USIM ApplicationのAIDが一致すれば他社のSIMでも動作する可能性があります。

> [!CAUTION]  
> このAppletはSIMを不可逆的に通信不能にする可能性があります。  
> 後述のバックアップを必ず実行し、生のIMSIを控えるようにしてください。  
> このAppletと付属のツールを使用してSIMが通信不能になっても自己責任です。

## ビルド

[IntelliJ IDEA](https://www.jetbrains.com/ja-jp/idea/) CommunityまたはUltimateに対応しています。  
スクリプトはLinuxまたはmacOS環境を前提としています。

> [!WARNING]  
> このリポジトリのサブモジュールに含まれるJCDKは、Oracle社の著作物です。  
> 利用する場合は、以下の利用規約に同意する必要があります。  
> https://github.com/martinpaljak/oracle_javacard_sdks/tree/master/jc305u4_kit/legal

1. このリポジトリをサブモジュールを含めてcloneする

```sh
git clone --recursive 'https://github.com/common-creation/sim-applet-imsi-test.git'
```

2. [lib/bootstrap.sh](./lib/bootstrap.sh)を実行して、UICC Toolkitなどをダウンロード・展開する

```sh
./lib/bootstrap.sh
```

3. `sample.env` を `.env` にコピーして `READER` を書き換える

4. `env/sample.key.env` を `env/key.env` にコピーして `ENC_KEY` `MAC_KEY` `KEK_KEY` を書き換える

5. IntelliJ IDEAで読み込む

6. `Build` を実行すると、 `./out/applet.cap` が生成される

![](https://i.imgur.com/pdPD9QB.png)

7. `./install.sh` を実行してCAPをeSIMにインストールする

## バックアップ・リストア

`./tool.sh` を使用すると、IMSIの確認・バックアップ・リストア・書き込みができます。  

### `./tool.sh read-current`

現在の生のIMSIと整形済みのIMSIを表示します。

### `./tool.sh read-backup`

バックアップ領域の生のIMSIと整形済みのIMSIを表示します。

### `./tool.sh backup`

バックアップ領域に生のIMSIを書き込みます。

> [!WARNING]  
> このバックアップ領域はAppletをアンインストール・再インストールすると破棄されます。

### `./tool.sh restore`

バックアップ領域から生のIMSIを書き戻します。

> [!CAUTION]  
> 生のIMSIの値を知らない状態で、バックアップ実行前にこのコマンドを実行すると、SIMが通信不能のまま復旧できなくなります。  
> `./tool.sh read-current` の値を書き留めておくか、 `./tool.sh read-backup` を実行し、確実にバックアップされていることを確認してください。

### `./tool.sh write <raw IMSI>`

指定した生のIMSIをSIMに書き込みます。  
Applet側から書き込みを行うので、ADMキーが無くても実行できます。

## QuickOpsを利用したビルド

[QuickOps](https://quickops.sh)を利用して、ビルド・アーティファクト保存をすることもできます。  
[.quickops.yaml](./.quickops.yaml)を同梱しているので、このリポジトリをGitHubまたはBacklog Gitにコピーするとすぐに利用できます。
