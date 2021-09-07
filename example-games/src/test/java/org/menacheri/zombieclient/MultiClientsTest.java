package org.menacheri.zombieclient;

import org.junit.Test;
import org.menacheri.jetclient.app.Session;
import org.menacheri.jetclient.app.impl.SessionFactory;
import org.menacheri.jetclient.util.LoginHelper;
import org.menacheri.jetclient.util.LoginHelper.LoginBuilder;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MultiClientsTest {

	private static final User user1 = new User("user1", "pass1");
	private static final User user2 = new User("user2", "pass2");
	private static final String ROOM = "Zombie_ROOM_1_REF_KEY_1";
	private static final String HOST = "localhost";

	@Test
	public void test() throws Exception {
		LoginHelper login1 = new LoginBuilder()
				.username(user1.username)
				.password(user2.password)
				.connectionKey(ROOM)
				.jetserverTcpHostName(HOST).tcpPort(18090)
				.build();

		SessionFactory factory1 = Optional.of(user1)
				.map(usr -> sessionFactory(usr.username, usr.password))
				.orElseThrow();

		SessionFactory factory2 = Optional.of(user2)
				.map(usr -> sessionFactory(usr.username, usr.password))
				.orElseThrow();


		Future<?> future1 = Executors.newSingleThreadExecutor()
				.submit(() -> {
					try {
						Session session1 = factory1.createAndConnectSession();
						Thread.sleep(1000 * 2);
						session1.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

		Future<?> future2 = Executors.newSingleThreadExecutor()
				.submit(() -> {
					try {
						Session session1 = factory2.createAndConnectSession();
						Thread.sleep(1000 * 2);
						session1.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

		while (!future1.isDone() || !future2.isDone()) {
			System.out.println("Wait...");
		}

	}

	private SessionFactory sessionFactory(String username, String password) {
		LoginHelper helper =  new LoginBuilder()
				.username(username)
				.password(password)
				.connectionKey(ROOM)
				.jetserverTcpHostName(HOST).tcpPort(18090)
				.build();

		try {
			return new SessionFactory(helper);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static class User {
		public String username;
		public String password;

		public User(String username, String password) {
			this.username = username;
			this.password = password;
		}
	}
}
