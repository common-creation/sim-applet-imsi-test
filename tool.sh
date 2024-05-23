#!/bin/bash

source .env
source "$LOAD_ENV"

APPLET_AID=A0000001157000000000000043433102

case $1 in
  read-current)
    gp \
      -d \
      -r "$READER" \
      --connect "$CONNECT_AID" \
      --key-enc "$ENC_KEY" \
      --key-mac "$MAC_KEY" \
      --key-dek "$KEK_KEY" \
      --applet "$APPLET_AID" \
      --apdu "8010000000" \
      | grep -A1 "80100000" | tail -1
    ;;
  read-backup)
    gp \
      -d \
      -r "$READER" \
      --connect "$CONNECT_AID" \
      --key-enc "$ENC_KEY" \
      --key-mac "$MAC_KEY" \
      --key-dek "$KEK_KEY" \
      --applet "$APPLET_AID" \
      --apdu "8012000000" \
      | grep -A1 "80120000" | tail -1
    ;;
  backup)
    gp \
      -d \
      -r "$READER" \
      --connect "$CONNECT_AID" \
      --key-enc "$ENC_KEY" \
      --key-mac "$MAC_KEY" \
      --key-dek "$KEK_KEY" \
      --applet "$APPLET_AID" \
      --apdu "8014000000" \
      | grep -A1 "80140000" | tail -1
    ;;
  restore)
    gp \
      -d \
      -r "$READER" \
      --connect "$CONNECT_AID" \
      --key-enc "$ENC_KEY" \
      --key-mac "$MAC_KEY" \
      --key-dek "$KEK_KEY" \
      --applet "$APPLET_AID" \
      --apdu "8016000000" \
      | grep -A1 "80160000" | tail -1
    ;;
  write)
    if [[ "$2" =~ ^[0-9]{18}$ ]]; then
      gp \
        -d \
        -r "$READER" \
        --connect "$CONNECT_AID" \
        --key-enc "$ENC_KEY" \
        --key-mac "$MAC_KEY" \
        --key-dek "$KEK_KEY" \
        --applet "$APPLET_AID" \
        --apdu "8018000009${2}" \
        | grep -A1 "80180000" | tail -1
    else
      echo "Usage: $0 write <raw IMSI>"
      exit 1
    fi
    ;;
  *)
    echo "Usage: $0 {read-current|read-backup|backup|restore|write}"
    exit 1
esac
