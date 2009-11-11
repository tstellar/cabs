
public abstract class Engine {

	int tlx;
	int tly;
	int width;
	int height;

	public abstract Cell findCell(int x, int y);

	public Engine(){}
	public Engine(int tlx, int tly, int width, int height) {
		this.tlx = tlx;
		this.tly = tly;
		this.width = width;
		this.height = height;
	}

	public Boolean hasCell(int x, int y) {
		return x >= tlx && y >= tly && x < tlx + width && y < tly + height;
	}
	
	public void setCoordinates(int tlx, int tly, int width, int height){
		this.tlx = tlx;
		this.tly = tly;
		this.width = width;
		this.height = height;
	}

}
