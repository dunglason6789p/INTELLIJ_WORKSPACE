/*
 * Decompiled with CFR 0.146.
 */
package vn.edu.hust.nlp.conll.model;

import java.io.PrintStream;
import vn.edu.hust.nlp.preprocess.StringUtils;

public class CONLLToken {
    private String id;
    private String form;
    private String lemma;
    private String uPOSTag;
    private String xPOSTag;
    private String feats;
    private String head;
    private String depRel;
    private String deps;
    private String misc;

    public CONLLToken() {
        this.id = "_";
        this.form = "_";
        this.lemma = "_";
        this.uPOSTag = "_";
        this.xPOSTag = "_";
        this.feats = "_";
        this.head = "_";
        this.depRel = "_";
        this.deps = "_";
        this.misc = "_";
    }

    public CONLLToken(String id, String form, String lemma, String uPOSTag, String xPOSTag, String feats, String head, String depRel, String deps, String misc) {
        this.id = id;
        if (id != "_" && !StringUtils.isNumeric(id)) {
            System.err.println("Warning: id must be a positive integer or _, not: " + id);
        }
        this.form = form;
        this.lemma = lemma;
        this.uPOSTag = uPOSTag;
        this.xPOSTag = xPOSTag;
        this.feats = feats;
        this.head = head;
        if (head != "_" && !StringUtils.isNumeric(head)) {
            System.err.println("Warning: head must be a positive integer string or _, not: " + head);
        }
        this.depRel = depRel;
        this.deps = deps;
        this.misc = misc;
    }

    public String toString() {
        String[] fields = new String[]{this.id, this.form, this.lemma, this.uPOSTag, this.xPOSTag, this.feats, this.head, this.depRel, this.deps, this.misc};
        return StringUtils.join(fields, "\t");
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getForm() {
        return this.form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getLemma() {
        return this.lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getuPOSTag() {
        return this.uPOSTag;
    }

    public void setuPOSTag(String uPOSTag) {
        this.uPOSTag = uPOSTag;
    }

    public String getxPOSTag() {
        return this.xPOSTag;
    }

    public void setxPOSTag(String xPOSTag) {
        this.xPOSTag = xPOSTag;
    }

    public String getFeats() {
        return this.feats;
    }

    public void setFeats(String feats) {
        this.feats = feats;
    }

    public String getHead() {
        return this.head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getDepRel() {
        return this.depRel;
    }

    public void setDepRel(String depRel) {
        this.depRel = depRel;
    }

    public String getDeps() {
        return this.deps;
    }

    public void setDeps(String deps) {
        this.deps = deps;
    }

    public String getMisc() {
        return this.misc;
    }

    public void setMisc(String misc) {
        this.misc = misc;
    }
}

