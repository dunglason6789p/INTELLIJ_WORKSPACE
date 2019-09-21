/*
 * Decompiled with CFR 0.146.
 */
package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import org.apache.commons.lang3.text.translate.AggregateTranslator;

public abstract class CharSequenceTranslator {
    public abstract int translate(CharSequence var1, int var2, Writer var3) throws IOException;

    public final String translate(CharSequence input) {
        if (input == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(input.length() * 2);
            this.translate(input, writer);
            return writer.toString();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public final void translate(CharSequence input, Writer out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (input == null) {
            return;
        }
        int sz = Character.codePointCount(input, 0, input.length());
        for (int i = 0; i < sz; ++i) {
            int consumed = this.translate(input, i, out);
            if (consumed == 0) {
                out.write(Character.toChars(Character.codePointAt(input, i)));
                continue;
            }
            for (int j = 0; j < consumed; ++j) {
                if (i < sz - 2) {
                    i += Character.charCount(Character.codePointAt(input, i));
                    continue;
                }
                ++i;
            }
            --i;
        }
    }

    public final CharSequenceTranslator with(CharSequenceTranslator ... translators) {
        CharSequenceTranslator[] newArray = new CharSequenceTranslator[translators.length + 1];
        newArray[0] = this;
        System.arraycopy(translators, 0, newArray, 1, translators.length);
        return new AggregateTranslator(newArray);
    }

    public static String hex(int codepoint) {
        return Integer.toHexString(codepoint).toUpperCase(Locale.ENGLISH);
    }
}

