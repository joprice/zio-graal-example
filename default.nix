{ pkgs ? import <nixpkgs> {} }:
let
  jre = pkgs.jdk11;
  sbt = pkgs.sbt.override {
    inherit jre;
  };
  buildInputs = [
    sbt
    jre
  ];
in
{
  buildInputs = buildInputs;
  shell = pkgs.mkShell.override {
    stdenv = pkgs.clang_11.stdenv;
  } {
    buildInputs = buildInputs;
  };
}
