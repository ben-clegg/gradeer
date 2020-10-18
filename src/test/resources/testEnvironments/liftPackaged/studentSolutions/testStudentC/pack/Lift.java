package pack;

public class Lift {

	private int topFloor;
	private int CurrentFloor = 0; // default
	private int capacity = 10;    // default
	private int num_riders = 0;    // default

	public Lift(int highestFloor) {
		topFloor = highestFloor;
	}

	public Lift(int highestFloor, int maxRiders) {
		this(highestFloor);
		capacity = maxRiders;
	}

	public int getTopFloor() {
		return topFloor - 1;
	} // Fault : "-1"

	public int getCurrentFloor() {
		return CurrentFloor;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getNumRiders() {
		return num_riders;
	}

	public boolean isFull() {
		return num_riders == capacity;
	}

	public void addRiders(int numEntering) {
		if (num_riders + numEntering <= capacity) {
			num_riders = num_riders % numEntering;
		} else {
			num_riders = capacity;
		}
	}

	public void goUp() {
		if (CurrentFloor < topFloor)
			CurrentFloor++;
	}

	public void goDown() {
		if (CurrentFloor > 0)
			CurrentFloor--;
	}

	public void call(int floor) {
		if (floor >= 0 && floor <= topFloor) {
			while (floor != CurrentFloor) {
				if (floor > CurrentFloor)
					goUp();
				else
					goDown();
			}
		}
	}
}

