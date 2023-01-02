package pack;

public class Lift {

  private int topFloor;

  // default
  private int currentFloor = 0;

  // default
  private int capacity = 10;

  // default
  private int numRiders = 0;

  public Lift(int highestFloor) {
    topFloor = highestFloor;
  }

  public Lift(int highestFloor, int maxRiders) {
    this(highestFloor);
    capacity = maxRiders;
  }

  public int getTopFloor() {
    return topFloor;
  }

  public int getCurrentFloor() {
    return currentFloor;
  }

  public int getCapacity() {
    return capacity;
  }

  public int getNumRiders() {
    return numRiders;
  }

  public boolean isFull() {
    return numRiders == capacity;
  }

  public void addRiders(int numEntering) {
    if (numRiders + numEntering <= capacity) {
      numRiders = numRiders + numEntering;
    } else {
      // Fault - value removed
    }
  }

  public void goUp() {
    if (currentFloor < topFloor)
      currentFloor++;
  }

  public void goDown() {
    if (currentFloor > 0)
      currentFloor--;
  }

  public void call(int floor) {
    if (floor >= 0 && floor <= topFloor) {
      while (floor != currentFloor) {
        if (floor > currentFloor)
          goUp();
        else
          goDown();
      }
    }
  }
}
