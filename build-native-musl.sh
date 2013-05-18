#!/bin/sh -e

CURR_DIR=`pwd`
TOP=$(dirname $0)/.
ROOT=$( (cd "$TOP" && pwd) )

VERSION=musl-0.9.10
FILE=$VERSION.tar.gz
URI=http://www.musl-libc.org/releases/$FILE

BUILD_DIR=/tmp/build
INSTALL_DIR=$ROOT/dependencies/native/unix

mkdir -p "$INSTALL_DIR"
mkdir -p "$BUILD_DIR"



cd "$BUILD_DIR"
test -f "$FILE" || wget "$URI"
test -d "$VERSION" || tar xzvf "$FILE"

cd "$VERSION"
make distclean
CC='gcc -m32' ./configure --prefix="$INSTALL_DIR/x86" --target=i586
make install

make distclean
CC='gcc -m64' ./configure --prefix="$INSTALL_DIR/x86_64" --target=x86_64
make install



cd "$CURR_DIR"

