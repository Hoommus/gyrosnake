package com.hoommus.gyrosnake.entities;

/**
 * x and y represent position in matrix, not physical
 */
public class SnakeSegment {
    private int x;
    private int y;

    public SnakeSegment(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void copyCoords(SnakeSegment other) {
        this.x = other.getX();
        this.y = other.getY();
    }
}
