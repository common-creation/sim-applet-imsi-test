#!/bin/bash -xe
cd "$(dirname $0)"

wget https://www.etsi.org/deliver/etsi_ts/102200_102299/102241/13.00.00_60/ts_102241v130000p0.zip
unzip -od ts_102241v130000p0 ts_102241v130000p0.zip
rm -f ts_102241v130000p0.zip
unzip -od ts_102241v130000p0/102241_Annex_B_Export_Files ts_102241v130000p0/102241_Annex_B_Export_Files.zip

wget https://www.3gpp.org/ftp/Specs/archive/31_series/31.130/31130-d30.zip
unzip -od 31130-d30 31130-d30.zip
rm -f 31130-d30.zip
unzip -od 31130-d30/31130_Annex_B_Export_Files 31130-d30/31130_Annex_B_Export_Files.zip
