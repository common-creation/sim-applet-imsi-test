#!/bin/bash -xe

source .env
source "$LOAD_ENV"

gp \
  -dv \
  -r "$READER" \
  --connect "$CONNECT_AID" \
  --key-enc "$ENC_KEY" \
  --key-mac "$MAC_KEY" \
  --key-dek "$KEK_KEY" \
  -uninstall "$CAP_PATH" \
  -force

gp \
  -dv \
  -r "$READER" \
  --connect "$CONNECT_AID" \
  --key-enc "$ENC_KEY" \
  --key-mac "$MAC_KEY" \
  --key-dek "$KEK_KEY" \
  -install "$CAP_PATH" \
  -params "$PARAMS"