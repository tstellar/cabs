package world;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public abstract class Agent {

	protected transient LocalCell cell;
	int turn = 0;
	public boolean hasMoved = false;

	public abstract void go();
	public abstract Color getColor();

	/*
	 * public void look(); public void reproduce(); public void die(); public
	 * void set(); public void read(); public void send(); public void recv();
	 */
	public void move(int x, int y) {
		cell.move(this, x, y);
	}

	public Collection<? extends Agent> look(int x, int y) {
		return cell.look(x, y);
	}

	public Agent look(int x, int y, String agentType){
		Collection<? extends Agent> list = look(x,y);
		for(Agent a : list){
			if(a.getClass().getName().contains(agentType)){
				return a;
			}
		}
		return null;
	}
	public void setCell(LocalCell cell) {
		this.cell = cell;
	}

	public void reproduce(){
		try{
			Class newClass = this.getClass();
			Agent newAgent = (Agent)newClass.newInstance();
			this.cell.add(newAgent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void die() {
		cell.remove(this);
	}

	public void start(int turn) {
		if (!hasMoved) {
			hasMoved = true;
			this.go();
		}
	}

	public byte[] toBytes() {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try {
			DataOutputStream out = new DataOutputStream(byteStream);
			Class c = this.getClass();
			Field[] fields = c.getDeclaredFields();
			ArrayList<Field> writeableFields = new ArrayList<Field>();
			for (Field f : fields) {
				int modifiers = f.getModifiers();
				if (Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers)) {
					continue;
				}
				writeableFields.add(f);
			}
			out.writeUTF(c.getName());
			out.writeInt(turn);
			out.writeInt(writeableFields.size());
			for (Field f : writeableFields) {
				Class t = f.getType();
				out.writeUTF(f.getName());
				String typeName = t.getName();
				if (typeName.equals("int")) {
					out.writeInt(f.getInt(this));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return byteStream.toByteArray();
	}

	public static Agent read(DataInputStream in) {
		Agent agent = null;
		try {
			String classname = in.readUTF();
			Class c = Class.forName(classname);
			agent = (Agent) c.newInstance();
			agent.turn = in.readInt();
			int numFields = in.readInt();
			for (int i = 0; i < numFields; i++) {
				Field f = c.getDeclaredField(in.readUTF());
				String fieldType = f.getType().getName();
				Object val = null;
				if (fieldType.equals("int")) {
					val = in.readInt();
				}
				f.set(agent, val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return agent;
	}
}
