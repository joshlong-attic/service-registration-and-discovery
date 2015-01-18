#!/bin/bash
set -e
curl http://localhost:8060/$1/photo -F "multipartFile=@$2" 
