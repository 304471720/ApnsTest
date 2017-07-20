start()
{
nohup  /www/3g.client.soufun.com/ljj/jdk7/bin/java -jar /logs/ljj/ApnsTest/target/ApnsTest-0.1.0.jar  >/dev/null 2>&1  &
}



logs()
{
tail -f  /logs/ljj/ApnsTest/ApnsTest.log
}

stop()
{
ps aux | grep ApnsTest | grep -v grep | awk '{print "sudo  kill "$2}' | sh
}


build()
{
stop
cd /logs/ljj/ApnsTest
export JAVA_HOME=/www/3g.client.soufun.com/ljj/jdk7
cvs up -d -C
mvn clean package
}
case "$1" in
   'start')
      start
      ;;
   'stop')
     stop
     ;;
   'logs')
     logs
     ;;
   'build')
     build
     ;;
  *)
     processnum
     exit 1
esac
exit 0
