/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.vnu.uet.liblinear;

final class IntArrayPointer {
    private final int[] _array;
    private int _offset;

    public void setOffset(int offset) {
        if (offset < 0 || offset >= this._array.length) {
            throw new IllegalArgumentException("offset must be between 0 and the length of the array");
        }
        this._offset = offset;
    }

    public IntArrayPointer(int[] array, int offset) {
        this._array = array;
        this.setOffset(offset);
    }

    public int get(int index) {
        return this._array[this._offset + index];
    }

    public void set(int index, int value) {
        this._array[this._offset + index] = value;
    }
}

