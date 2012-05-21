#!/bin/bash
sass readme.scss:readme.css
pandoc --template template.html5 --toc -s -S -c readme/readme.css -t html5 readme.md -o ../readme.html

