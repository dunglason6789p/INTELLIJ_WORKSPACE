/*
 * Decompiled with CFR 0.146.
 */
package third_party.org.chokkan.crfsuite;

import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;
import third_party.org.chokkan.crfsuite.crfsuiteJNI;

public class Tagger {
    private long swigCPtr;
    protected boolean swigCMemOwn;

    public Tagger(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    public static long getCPtr(Tagger obj) {
        return obj == null ? 0L : obj.swigCPtr;
    }

    protected void finalize() {
        this.delete();
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0L) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                crfsuiteJNI.delete_Tagger(this.swigCPtr);
            }
            this.swigCPtr = 0L;
        }
    }

    public Tagger() {
        this(crfsuiteJNI.new_Tagger(), true);
    }

    public boolean open(String name) {
        return crfsuiteJNI.Tagger_open(this.swigCPtr, this, name);
    }

    public void close() {
        crfsuiteJNI.Tagger_close(this.swigCPtr, this);
    }

    public StringList labels() {
        return new StringList(crfsuiteJNI.Tagger_labels(this.swigCPtr, this), true);
    }

    public StringList tag(ItemSequence xseq) {
        return new StringList(crfsuiteJNI.Tagger_tag(this.swigCPtr, this, ItemSequence.getCPtr(xseq), xseq), true);
    }

    public void set(ItemSequence xseq) {
        crfsuiteJNI.Tagger_set(this.swigCPtr, this, ItemSequence.getCPtr(xseq), xseq);
    }

    public StringList viterbi() {
        return new StringList(crfsuiteJNI.Tagger_viterbi(this.swigCPtr, this), true);
    }

    public double probability(StringList yseq) {
        return crfsuiteJNI.Tagger_probability(this.swigCPtr, this, StringList.getCPtr(yseq), yseq);
    }

    public double marginal(String y, int t) {
        return crfsuiteJNI.Tagger_marginal(this.swigCPtr, this, y, t);
    }
}

