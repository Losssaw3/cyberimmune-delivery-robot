#!/usr/bin/env bash

rm res/*
shopt -s globstar
plantuml ./**/*.puml
mv ./**/*.png res/