package com.itwillbs.chat.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class ChatDAO {
	
//	DataSource dataSource;
//	private Connection conn;
		public Connection getConnection() throws Exception{
			InitialContext initContext = new InitialContext();
//			Context envContext = (Context) initContext.lookup("java:/comp/env");
//			dataSource = (DataSource) envContext.lookup("java:comp/env/jdbc/MysqlDB");
			Context init=new InitialContext();
			DataSource ds=(DataSource)init.lookup("java:comp/env/jdbc/MysqlDB");
			Connection con=ds.getConnection();
			return con;
		} 
	
	
	// 특정 ID에 따라 채팅 내역 가져오기
	public ArrayList<ChatDTO> getChatListByID(String from_id, String to_id, String chat_id){
		ArrayList<ChatDTO> chatList = null; // 하나하나의 메시지를 리스트에 담아 보관
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String SQL = "SELECT * FROM chat WHERE ((from_id = ? AND to_id = ?) OR (from_id = ? AND to_id = ?)) AND chat_id > ? ORDER BY chat_time";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, from_id);
			pstmt.setString(2, to_id);
			pstmt.setString(3, to_id);
			pstmt.setString(4, from_id);
			pstmt.setInt(5, Integer.parseInt(chat_id)); 
			rs = pstmt.executeQuery();
			chatList = new ArrayList<ChatDTO>();
			while(rs.next()) {
				ChatDTO chat = new ChatDTO();
				chat.setchat_id(rs.getInt("chat_id"));
				chat.setfrom_id(rs.getString("from_id").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>")); // sql인젝션, 스크립트 공격방지
				chat.setto_id(rs.getString("to_id").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>")); // sql인젝션, 스크립트 공격방지
				chat.setchat_content(rs.getString("chat_content").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>"));
				int chat_time = Integer.parseInt(rs.getString("chat_time").substring(11, 13));
				String timeType = "오전";
				if(chat_time > 12) {
					timeType = "오후";
					chat_time -= 12;
				}
				chat.setchat_time(rs.getString("chat_time").substring(0, 11) + " " + timeType + " " + chat_time + ":" + rs.getString("chat_time").substring(14, 16) + "");
				chatList.add(chat);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return chatList; // 리스트 반환
	}
	
	//대화 내역중 최근에 number개까지 뽑아줌
	public ArrayList<ChatDTO> getChatListByRecent(String from_id, String to_id, int number){
		ArrayList<ChatDTO> chatList = null; // 하나하나의 메시지를 리스트에 담아 보관
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String SQL = "SELECT * FROM chat WHERE ((from_id = ? AND to_id = ?) OR (from_id = ? AND to_id = ?)) AND chat_id > (SELECT MAX(chat_id) - ? FROM chat WHERE (from_id = ? AND to_id = ?) OR (from_id = ? AND to_id = ?)) ORDER BY chat_time";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, from_id);
			pstmt.setString(2, to_id);
			pstmt.setString(3, to_id);
			pstmt.setString(4, from_id);
			pstmt.setInt(5, number); 
			pstmt.setString(6, from_id);
			pstmt.setString(7, to_id);
			pstmt.setString(8, to_id);
			pstmt.setString(9, from_id);
			rs = pstmt.executeQuery();
			chatList = new ArrayList<ChatDTO>();
			while(rs.next()) {
				ChatDTO chat = new ChatDTO();
				chat.setchat_id(rs.getInt("chat_id"));
				chat.setfrom_id(rs.getString("from_id").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>")); // sql인젝션, 스크립트 공격방지
				chat.setto_id(rs.getString("to_id").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>")); // sql인젝션, 스크립트 공격방지
				chat.setchat_content(rs.getString("chat_content").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>"));
				int chat_time = Integer.parseInt(rs.getString("chat_time").substring(11, 13));
				String timeType = "오전";
				if(chat_time > 12) {
					timeType = "오후";
					chat_time -= 12;
				}
				chat.setchat_time(rs.getString("chat_time").substring(0, 11) + " " + timeType + " " + chat_time + ":" + rs.getString("chat_time").substring(14, 16) + "");
				chatList.add(chat);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return chatList; // 리스트 반환
	}
	
	
	public ArrayList<ChatDTO> getBox(String mem_id){
		ArrayList<ChatDTO> chatList = null; // 하나하나의 메시지를 리스트에 담아 보관
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String SQL = "SELECT * FROM chat WHERE chat_id IN (SELECT MAX(chat_id) FROM chat WHERE to_id = ? OR from_id = ? GROUP BY from_id, to_id)";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, mem_id);
			pstmt.setString(2, mem_id);
			rs = pstmt.executeQuery();
			chatList = new ArrayList<ChatDTO>();
			while(rs.next()) {
				ChatDTO chat = new ChatDTO();
				chat.setchat_id(rs.getInt("chat_id"));
				chat.setfrom_id(rs.getString("from_id").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>")); // sql인젝션, 스크립트 공격방지
				chat.setto_id(rs.getString("to_id").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>")); // sql인젝션, 스크립트 공격방지
				chat.setchat_content(rs.getString("chat_content").replaceAll(" ", "&nbsp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>"));
				int chat_time = Integer.parseInt(rs.getString("chat_time").substring(11, 13));
				String timeType = "오전";
				if(chat_time > 12) {
					timeType = "오후";
					chat_time -= 12;
				}
				chat.setchat_time(rs.getString("chat_time").substring(0, 11) + " " + timeType + " " + chat_time + ":" + rs.getString("chat_time").substring(14, 16) + "");
				chatList.add(chat);
			}
			for(int i = 0; i < chatList.size(); i++) {
				ChatDTO x = chatList.get(i);
				for(int j = 0; j < chatList.size(); j++) {
					ChatDTO y = chatList.get(j);
					if(x.getfrom_id().equals(y.getto_id()) && x.getto_id().equals(y.getfrom_id())) {
						if(x.getchat_id() < y.getchat_id()) {
							//더 최신일 때 제외하고는 메시지 지워줌
							chatList.remove(x);
							i--;
							break;
						}else {
							chatList.remove(y);
							j--;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return chatList; // 리스트 반환
	}
	
	
	
	
	
	//채팅전송
	public int submit(String from_id, String to_id, String chat_content){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String SQL = "INSERT INTO chat VALUES (NULL, ?, ?, ?, NOW(), 0)";
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, from_id);
			pstmt.setString(2, to_id);
			pstmt.setString(3, chat_content);
			return pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return -1; // 반환될 수 없는 값을 넣어주므로 데이터베이스 오류 알려줌
	}
	
	public int readChat(String from_id, String to_id) { //읽음처리함수
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String SQL = "UPDATE chat SET chat_read = 1 WHERE (from_id = ? AND to_id = ?)";
		try {
			conn=getConnection();
			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, to_id);
			pstmt.setString(2, from_id);
			return pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	return -1; // 데이터베이스 오류
	}
	
	
	public int getAllUnreadChat(String mem_id) { //현재 읽지않은 모든 메시지 갯수
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String SQL = "SELECT COUNT(chat_id) FROM chat WHERE to_id = ? AND chat_read = 0";
		try {
			conn=getConnection();
			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, mem_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				return rs.getInt("COUNT(chat_id)");
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	return -1; // 데이터베이스 오류
	}
	
	
	public int getUnreadChat(String from_id, String to_id) { // 특정 사용자와의 채팅 메시지를 얼마나 읽지않았는지 출력
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String SQL = "SELECT COUNT(chat_id) FROM chat WHERE from_id = ? AND to_id = ? AND chat_read = 0";
		try {
			conn=getConnection();
			pstmt = conn.prepareStatement(SQL);
			pstmt.setString(1, from_id);
			pstmt.setString(2, to_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				return rs.getInt("COUNT(chat_id)");
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null) rs.close();
				if(pstmt != null) pstmt.close();
				if(conn != null) conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
	return -1; // 데이터베이스 오류
	}
	
	   public String getNickname(String mem_id) { //닉네임 불러오기
		      String nickname = null;
		      Connection con = null;
		      PreparedStatement pstmt = null;
		      ResultSet rs = null;
		      try {   
		         con = getConnection();
		         
//		         String sql="select mem_id, nickname from members m join market mr where m.mem_id = mr.insert_id and mr.market_id=?";
		         String sql="select mem_id, nickname from members where mem_id=?";
		         pstmt=con.prepareStatement(sql);
		         pstmt.setString(1, mem_id);  
		         
		         rs=pstmt.executeQuery();
		         
		         if(rs.next()) {
		            nickname = rs.getString("nickname");
		         }
		      } catch (Exception e) {
		         e.printStackTrace();
		      } finally {
		         if(pstmt!=null) try { pstmt.close();} catch (Exception e2) {}
		         if(con!=null) try { con.close();} catch (Exception e2) {}
		         if(rs!=null) try { rs.close();} catch (Exception e2) {}
		      }
		      return nickname;
		   }
	
	
} // 클래스 
