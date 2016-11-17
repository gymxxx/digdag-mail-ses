# digdag-mail-ses

digdag mail plugin for Amazon Email Service

## How to use

### Create file
[test.dig]
```
timezone: Asia/Tokyo
_export:
  plugin:
    repositories:
      - https://dl.bintray.com/gymxxx/maven/
    dependencies:
      - io.digdag.plugin.digdag-mail-ses:0.1.0

+step0:
  mail_ese>: content.txt
  subject: test_subject
  sender: test_from@example.com
  to: [test_to@example.com]
  use_html: false
  
```

### Run

```
$ digdag run test.dig
```
