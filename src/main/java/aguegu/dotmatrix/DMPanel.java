package aguegu.dotmatrix;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;

class DMPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = -2531292225634588108L;
	private DotMatrix dm;
	private DMImage[] dmi;
	private DMMode mode;
	private boolean leftPressed;
	private int lastBlockID, lastBlockColumn, lastBlockRow, lastRow;
	private int highlight = -1;

	public DMPanel() {
		this.setSize(DMImage.getBlockWidth() * (9 * 8 + 1),
				DMImage.getBlockWidth() * (9 * 3 + 1));

		this.setPreferredSize(new Dimension(DMImage.getBlockWidth()
				* (9 * 8 + 1), DMImage.getBlockWidth() * (9 * 3 + 1)));

		dm = new DotMatrix();
		dmi = new DMImage[24];
		for (int i = 0; i < dmi.length; i++) {
			dmi[i] = new DMImage();
		}

		mode = DMMode.XYZ;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		init();
	}

	public void setDotMatrix(DotMatrix dm) {
		this.dm = dm;
	}

	public DotMatrix getDotMatrix() {
		return this.dm;
	}

	private int getIndex(int row, int blockID, int blockC, int blockR) {
		int index = -1;

		switch (this.mode) {
		case YZX:
			switch (row) {
			case 0:
				index = blockID * 64 + blockC * 8 + blockR;
				break;
			case 1:
				index = (7 - blockC) * 64 + blockID * 8 + blockR;
				break;
			case 2:
				index = (7 - blockR) * 64 + blockC * 8 + blockID;
				break;
			}
			break;
		case ZXY:
			switch (row) {
			case 0:
				index = blockC * 64 + blockR * 8 + blockID;
				break;
			case 1:
				index = blockID * 64 + blockR * 8 + 7 - blockC;
				break;
			case 2:
				index = blockC * 64 + blockID * 8 + 7 - blockR;
				break;
			}
			break;
		case XYZ:
		default:
			switch (row) {
			case 0:
				index = blockR * 64 + blockID * 8 + blockC;
				break;
			case 1:
				index = blockR * 64 + (7 - blockC) * 8 + blockID;
				break;
			case 2:
				index = blockID * 64 + (7 - blockR) * 8 + blockC;
				break;
			}
			break;
		}

		return index;
	}

	private void init() {
		this.update();
	}

	public void update() {
		for (int i = 0; i < DotMatrix.DOT_LENGTH; i++) {
			int x, y, z;
			switch (mode) {

			case YZX:
				z = i % 8;
				x = i / 8 % 8;
				y = i / 64;
				break;
			case ZXY:
				y = i % 8;
				z = i / 8 % 8;
				x = i / 64;
				break;
			case XYZ:
			default:
				x = i % 8;
				y = i / 8 % 8;
				z = i / 64;
				break;
			}

			boolean val = dm.getDot(i);

			dmi[y].setDot(z * 8 + x, val);
			dmi[8 + x].setDot(z * 8 + 7 - y, val);
			dmi[16 + z].setDot((7 - y) * 8 + x, val);
			dmi[y].setHighlight(z * 8 + x, i == highlight);
			dmi[8 + x].setHighlight(z * 8 + 7 - y, i == highlight);
			dmi[16 + z].setHighlight((7 - y) * 8 + x, i == highlight);
		}

		for (DMImage image : dmi) {
			image.update();
		}
	}

	private void updateHighlight(int index) {
		if (index == highlight) return;
		highlight = index;
		update();
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.lightGray);
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

		for (int r = 0, y = DMImage.getBlockWidth(); r < 3; r++, y += DMImage
				.getBlockWidth() * 9) {
			for (int c = 0, x = DMImage.getBlockWidth(); c < 8; c++, x += DMImage
					.getBlockWidth() * 9) {
				int index = r * 8 + c;
				g2d.drawImage(dmi[index], x, y, null);
			}
		}
	}

	public void setMode(DMMode mode) {
		this.mode = mode;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			int blockID = getBlockID(e.getX());
			int blockColumn = getBlockColumn(e.getX());
			int blockRow = getBlockRow(e.getY());
			int row = getRow(e.getY());

			if (blockID >= 8 || blockColumn < 0 || blockRow < 0)
				return;

			int index = getIndex(row, blockID, blockColumn, blockRow);

			if (e.isShiftDown()) {
				dm.setDot(index, true);
			} else if (e.isControlDown()) {
				dm.setDot(index, false);
			}else {
				dm.reverseDot(index);
			}

			leftPressed = true;
			lastBlockID = blockID;
			lastBlockColumn = blockColumn;
			lastBlockRow = blockRow;
			lastRow = row;

			update();
			repaint();
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			int blockID = getBlockID(e.getX());

			if (blockID >= 8 || getBlockColumn(e.getX()) < 0 || getBlockRow(e.getY()) < 0)
				return;

			for (int x = 0 ; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					int index = getIndex(getRow(e.getY()), blockID, x, y);

					if (e.isShiftDown()) {
						dm.setDot(index, true);
					} else if (e.isControlDown()) {
						dm.setDot(index, false);
					}else {
						dm.reverseDot(index);
					}
				}
			}

			update();
			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			leftPressed = false;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int blockID = getBlockID(e.getX());
		int blockColumn = getBlockColumn(e.getX());
		int blockRow = getBlockRow(e.getY());
		int row = getRow(e.getY());

		if (blockID >= 8 || blockColumn < 0 || blockRow < 0) {
			updateHighlight(-1);
			return;
		}

		int index = getIndex(getRow(e.getY()), blockID, blockColumn, blockRow);

		if (leftPressed) {
			if (e.isAltDown()) {
				updateHighlight(index);
				return;
			}

			if (blockID == lastBlockID && blockColumn == lastBlockColumn && blockRow == lastBlockRow && row == lastRow) {
				updateHighlight(index);
				return;
			}

			highlight = index;

			if (e.isShiftDown()) {
				dm.setDot(index, true);
			} else if (e.isControlDown()) {
				dm.setDot(index, false);
			}else {
				dm.reverseDot(index);
			}

			lastBlockID = blockID;
			lastBlockColumn = blockColumn;
			lastBlockRow = blockRow;
			lastRow = row;

			update();
			repaint();
		} else {
			updateHighlight(index);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int blockID = getBlockID(e.getX());
		int row = getRow(e.getY());

		if (blockID >= 8 || getBlockColumn(e.getX()) < 0 || getBlockRow(e.getY()) < 0)
			return;

		boolean[][] oldData = new boolean[8][8];

		// load
		for (int x = 0 ; x < 8; x++)
			for (int y = 0; y < 8; y++)
				oldData[x][y] = dm.getDot(getIndex(row, blockID, x, y));

		// rotate
		if (e.getWheelRotation() < 0) {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					dm.setDot(getIndex(row, blockID, i, j), oldData[7 - j][i]);
				}
			}
		} else if (e.getWheelRotation() > 0) {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					dm.setDot(getIndex(row, blockID, i, j), oldData[j][7 - i]);
				}
			}
		}

		update();
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int blockID = getBlockID(e.getX());
		int blockColumn = getBlockColumn(e.getX());
		int blockRow = getBlockRow(e.getY());
		int row = getRow(e.getY());

		updateHighlight(blockID >= 8 || blockColumn < 0 || blockRow < 0 ? -1 :getIndex(row, blockID, blockColumn, blockRow));
	}

	private static int getBlockColumn(int x) {
		return (x / DMImage.getBlockWidth()) % 9 - 1;
	}

	private static int getBlockRow(int y) {
		return (y / DMImage.getBlockWidth()) % 9 - 1;
	}

	private static int getBlockID(int x) {
		return (x / DMImage.getBlockWidth()) / 9;
	}

	private static int getRow(int y) {
		return (y / DMImage.getBlockWidth()) / 9;
	}
}
