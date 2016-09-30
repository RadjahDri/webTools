import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class DBconnection {
	private final static String pasteTableName = "paste";
	private final static String emailTableName = "email";
	
	/**
	 * Table for paste
	 */
	private MongoCollection<Document> pasteTable;
	/**
	 * Table for email found in paste
	 */
	private MongoCollection<Document> emailTable;
	
	public DBconnection(String database){
		MongoDatabase db = new MongoClient().getDatabase(database);
		pasteTable = db.getCollection(pasteTableName);
		emailTable = db.getCollection(emailTableName);
	}
	
	public void insertEmail(String email, String pasteId){
		Document doc = new Document();
		doc.append("email", email);
		doc.append("paste", pasteId);
		
		emailTable.insertOne(doc);
	}
	
	public void insertPaste(String title, String author, String date, String content){
		Document doc = new Document();
		doc.append("title", title);
		doc.append("author", author);
		doc.append("date", date);
		doc.append("content", content);
		
		pasteTable.insertOne(doc);
	}
}
