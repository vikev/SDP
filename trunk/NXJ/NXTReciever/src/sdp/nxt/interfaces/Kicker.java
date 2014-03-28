package sdp.nxt.interfaces;

public interface Kicker {

	/**
	 * Close the grabber. Should do nothing if it's closed.
	 */
	public void grab();

	/**
	 * Kick with specified power. Should do nothing if the grabber is not
	 * closed.
	 * 
	 * @param power
	 */
	public void kick(int power);

	/**
	 * Kick with maximum power. Should do nothing if the grabber is not closed.
	 */
	public void kick();

	/**
	 * Check if the grabber is closed.
	 * 
	 * @return
	 */
	public boolean isClosed();

	public void open();
}
