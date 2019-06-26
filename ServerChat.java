package chat1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import sun.util.locale.StringTokenIterator;

public class ServerChat extends JFrame implements ActionListener {
	private JPanel contentPane;
	private JTextField porttextfield;
	private JTextArea textArea = new JTextArea();
	private JButton startbtn = new JButton("��������");
	private JButton exitbtn = new JButton("��������");
	//net work
	private ServerSocket ss;
	private Socket s;
	private int port;
	private Vector uservc = new Vector();
	private StringTokenizer st; 
	

	ServerChat() { // ������
		init();// ȭ�� ���� �޼ҵ�
		start();

	}

	private void start() {
		startbtn.addActionListener(this);
		exitbtn.addActionListener(this);

	}

	public void Server_start() {
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (ss != null) {
			connect();

		}

	}

	private void connect() {

		Thread th = new Thread(new Runnable() {// inner class

			@Override
			public void run() { // ������ ������
				while(true) {
					
				try {
					textArea.append("�����\n");
					s = ss.accept();
					textArea.append("���ӿϷ�");
					
					UserInfo user = new UserInfo(s);
					
					user.start();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				}//while end 

			}
		});
		th.start();
	}

	private void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(57, 30, 314, 172);
		contentPane.add(scrollPane);

		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("��Ʈ��ȣ");
		lblNewLabel.setBounds(12, 202, 71, 49);
		contentPane.add(lblNewLabel);
 
		porttextfield = new JTextField();
		porttextfield.setBounds(98, 216, 174, 35);
		contentPane.add(porttextfield);
		porttextfield.setColumns(10);

		startbtn.setBounds(284, 215, 97, 23);
		contentPane.add(startbtn);

		exitbtn.setBounds(284, 238, 97, 23);
		contentPane.add(exitbtn);

		this.setVisible(true);// Ʈ���ϰ�� ȭ�鿡����

	}

	public static void main(String[] args) {
		new ServerChat();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startbtn) {
			System.out.println("��ŸƮ ��ư Ŭ��");
			port = Integer.parseInt(porttextfield.getText().trim());
			Server_start();// ��ư������ ���� ����

		} else if (e.getSource() == exitbtn) {
			System.out.println("���� ��ư Ŭ��");
		}
	}//�̺�Ʈ
	
	class UserInfo extends Thread{ 
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		
		private Socket user_socket;
		private String Nickname = "";
		
		public UserInfo(Socket soc) { //������ �Լ� 
			this.user_socket = soc;
			
			UserNetwork();
		}
		
		public void UserNetwork(){ 
			try {
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);
				
				
				Nickname = dis.readUTF();
				textArea.append(Nickname + " ����� ���� ");
				
				//���ε��� ����� �߰� 
				Broad("NewUser/"+ Nickname);
				//�ڽſ��� �޾ƿ��� .
				for(int i = 0; i < uservc.size(); i++) {
					UserInfo u = (UserInfo)uservc.elementAt(i);
					sendM("OldUser/" + u.Nickname);
				}
				
				uservc.add(this);
				
				Broad("user_list_update/" );
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		public void Broad(String str) {
			for(int i = 0; i< uservc.size(); i++ ) {// ��ü ����ڿ��� �޼��� 
				UserInfo u = (UserInfo)uservc.elementAt(i);
				u.sendM(str);
				
				//����ڵ��� ��������� ���Ϳ��� ����ڸ� �ϳ� ���� userinfo������ ��ȯ�Ͽ� �Ѹ��� ���� 
			} 
			
		}
		public void run() { //thread ���� ó�� 
			while(true) {
				try {
					String msg = dis.readUTF(); //�޼�������
					textArea.append(Nickname + "���� ���� ���� �޼���" + msg);
					Inmsg(msg);
				} catch (IOException e) {
					e.printStackTrace();
				} //�޼��� ���� 
				
				
			}//while end 
		}
		
		public void Inmsg(String str) {
			st = new StringTokenizer(str, "/");
			
			String protocol =st.nextToken();
			String message = st.nextToken();
			System.out.println("��������   : " + protocol);
			System.out.println("�޼���   : " + message);
			
			
			if(protocol.equals("Note"))
			{
			
					String note = st.nextToken();
				
				System.out.println("�޴� ���  : " + message);
				System.out.println("���� ����  : "  +  note);
				for(int i = 0; i <uservc.size(); i ++) {
					UserInfo u = (UserInfo)uservc.elementAt(i);
					if(u.Nickname.equals(message)) {
						u.sendM("Note" + Nickname+"@"+note);
					}
				}
			
			}
			
			
		}
		
		private void sendM(String str) {
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}

}