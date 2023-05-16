if [[ $OSTYPE == 'darwin'* ]]; then
  find . -type f -name "*.class" -exec rm {} \;
  find . -type d -name "output" -exec rm -fR {} \;
else
  rm */**/*.class
  rm -r */**/output
fi