package org.menacheri.zombieclient;

import org.junit.Test;
import org.menacheri.jetclient.app.Session;
import org.menacheri.jetclient.app.impl.SessionFactory;
import org.menacheri.jetclient.event.Event;
import org.menacheri.jetclient.event.SessionEventHandler;
import org.menacheri.jetclient.event.impl.AbstractSessionEventHandler;
import org.menacheri.jetclient.util.LoginHelper;
import org.menacheri.jetclient.util.LoginHelper.LoginBuilder;
import org.menacheri.jetclient.communication.NettyMessageBuffer;
import org.menacheri.zombie.domain.IAM;

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
		SessionFactory factory1 = Optional.of(user1)
				.map(usr -> sessionFactory(usr.username, usr.password))
				.orElseThrow();

		SessionFactory factory2 = Optional.of(user2)
				.map(usr -> sessionFactory(usr.username, usr.password))
				.orElseThrow();


		Session session1 = factory1.createAndConnectSession(new TestHandler());
		for (int i = 0; i < 5; i++) {
			new GamePlay(IAM.ZOMBIE, session1).run();
			Thread.sleep(1000 * 2);
		}
		Thread.sleep(1000 * 10);
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

	private static class TestHandler extends AbstractSessionEventHandler {

		@Override
		public void onDataIn(Event event) {
			if (event.getSource() instanceof NettyMessageBuffer) {
				NettyMessageBuffer buffer = (NettyMessageBuffer) event.getSource();
				System.out.printf("Event [%d] from server: %d\n", event.getType(), buffer.readInt());
			} else {
				System.out.println(event);
			}
		}
	}
}
