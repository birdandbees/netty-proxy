import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created with IntelliJ IDEA.
 * User: jingjing
 * Date: 10/29/13
 * Time: 9:43 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JUnit4.class)
public class DelayProxyTest {
    private static TimestampServer testServer;


    public static void checkTestPortsAreAvailable() throws IOException, InterruptedException {

        String[] cmd = {"/bin/sh", "-c", "netstat -nat | grep ':9001|:9002|:8999|:8998'"};
        Process check = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));
        if (br.readLine() != null) {

            throw new IOException("ports are not available");
        }

    }

    @BeforeClass
    public static void setUp() throws IOException, InterruptedException, Exception {
        checkTestPortsAreAvailable();
        testServer = new TimestampServer(9002);
        testServer.run();

    }

    @AfterClass
    public static void tearDown() throws IOException {

        testServer.shutDown();
        String[] cmd = {"/bin/sh", "-c", "kill -9 $(lsof -i:9001 -i:8999 -i:8998 -t)"};
        Runtime.getRuntime().exec(cmd);

    }

    @Test
    public void testNewDelayProxy() throws Exception {
        DelayProxy delayProxy = new DelayProxy(9001, 9002);
        LatencyTestClient testClient = new LatencyTestClient("localhost", 9002);
        testClient.run(0, 1);


    }

    @Test
    public void testDelayProxyCanStart() throws Exception {

        DelayProxy delayProxy = new DelayProxy(9001, 9002);
        LatencyTestClient testClient = new LatencyTestClient("localhost", 9002);
        delayProxy.start();
        testClient.run(0, 0);
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
    public void testDelayProxyCanRelay() throws Exception {
        DelayProxy delayProxy = new DelayProxy(8998, 9002);
        LatencyTestClient testClient = new LatencyTestClient("localhost", 8998);
        delayProxy.start();
        delayProxy.delay(0);
        testClient.run(0, 0);
        delayProxy.shutDown();
    }

    @Test
    public void testDelayProxyCanDelay() {
        DelayProxy delayProxy = new DelayProxy(9001, 9002);
        try {
            delayProxy.start();
            LatencyTestClient testClient = new LatencyTestClient("localhost", 9001);
            delayProxy.delay(5);
            testClient.run(5, 1);

        } catch (Exception e) {
            fail();
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
    public void testEquals() {
        DelayProxy delayProxy1 = new DelayProxy(9000, 9002);
        DelayProxy delayProxy2 = new DelayProxy(9000, 9110);
        assertTrue(delayProxy1.equals(delayProxy2));
    }


}
