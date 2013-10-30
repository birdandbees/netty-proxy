import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: jingjing
 * Date: 10/29/13
 * Time: 9:43 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JUnit4.class)
public class DelayProxyTest {

    public static void checkTestPortsAreAvailable() throws IOException, InterruptedException {

        String[] cmd = { "/bin/sh", "-c", "netstat -nat | grep ':9001|:9002|:8999|:8998'" };
        Process check = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));
        if (  br.readLine() != null )
        {

            throw new IOException("ports are not available");
        }

    }

    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        checkTestPortsAreAvailable();
        ProcessBuilder pb = new ProcessBuilder("redis-server", "--port 9002");
        pb.start();

    }

    @AfterClass
    public static void tearDown() throws IOException {
        String[] cmd = { "/bin/sh", "-c", "kill -9 $(lsof -i:9002 -t)" };
        Runtime.getRuntime().exec(cmd);

    }

    @Test
    public void testNewDelayProxy() {
        DelayProxy delayProxy = new DelayProxy(9001, 9002);
        Jedis jedis = new Jedis("localhost", 9002);
        jedis.flushAll();
        jedis.set("american_horror_story", "Ryan_Murphy");
        String author = jedis.get("american_horror_story");
        assert (author).equals("Ryan_Murphy");

    }

    @Test
    public void testDelayProxyCanStart() throws InterruptedException {

        DelayProxy delayProxy = new DelayProxy(9001, 9002);
        Jedis jedis = new Jedis("localhost", 9002);
        delayProxy.start();
        jedis.flushAll();
        jedis.set("dracular", "Bram_Stoker");
        String author = jedis.get("dracular");
        assert (author).equals("Bram_Stoker");
        delayProxy.shutDown();
    }

    @Test
    public void testDelayProxyCanShutDown() {
        DelayProxy delayProxy = new DelayProxy(8999, 9002);
        delayProxy.start();
        delayProxy.delay(0);
        delayProxy.shutDown();
        assertTrue(delayProxy.isShutDown());
    }

    @Test
    public void testDelayProxyCanRelay() {
        DelayProxy delayProxy = new DelayProxy(8998, 9002);
        delayProxy.start();
        Jedis jedis = new Jedis("localhost", 8998);
        delayProxy.delay(0);
        jedis.flushAll();
        jedis.set("dracular", "Bram_Stoker");
        String author = jedis.get("dracular");
        assert (author).equals("Bram_Stoker");
        delayProxy.shutDown();
    }

    @Test(expected = redis.clients.jedis.exceptions.JedisConnectionException.class)
    public void testDelayProxyCanDelay() {
        DelayProxy delayProxy = new DelayProxy(9001, 9002);
        try {
            delayProxy.start();
            Jedis jedis = new Jedis("localhost", 9001);
            delayProxy.delay(20);
            jedis.flushAll();
            //jedis.set("dracular", "Bram_Stoker");
            //String author = jedis.get("dracular");
        } finally {
            delayProxy.shutDown();
        }
    }

    @Test
    public void testDelayProxyCantStartOnUsedPort() {
        DelayProxy delayProxy = new DelayProxy(8999, 9002);
        DelayProxy dupDelayProxy = new DelayProxy(8999, 9002);
        try {

            delayProxy.start();
            delayProxy.delay(0);

            dupDelayProxy.start();
            dupDelayProxy.delay(20);

        } catch (Exception e) {
            assert (e.getMessage().contains("java.net.BindException"));
        } finally {
            delayProxy.shutDown();
            dupDelayProxy.shutDown();
        }


    }
    @Test
    public void testEquals()
    {
        DelayProxy delayProxy1 = new DelayProxy(9000, 9002);
        DelayProxy delayProxy2 = new DelayProxy(9000, 9110);
        assertTrue(delayProxy1.equals(delayProxy2));
    }


}
