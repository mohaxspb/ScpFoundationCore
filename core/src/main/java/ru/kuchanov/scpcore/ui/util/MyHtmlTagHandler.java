package ru.kuchanov.scpcore.ui.util;

import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.UnderlineSpan;

import org.xml.sax.XMLReader;

import java.util.Stack;

import timber.log.Timber;


public class MyHtmlTagHandler implements TagHandler {
    /**
     * List indentation in pixels. Nested lists use multiple of this.
     */
    private static final int indent = 10;
    private static final int listItemIndent = indent * 2;
    private static final BulletSpan bullet = new BulletSpan(indent);
    /**
     * Keeps track of lists (ol, ul). On bottom of Stack is the outermost list
     * and on top of Stack is the most nested list
     */
    private final Stack<String> lists = new Stack<>();

    /**
     * Tracks indexes of ordered lists so that after a nested list ends
     * we can continue with correct index of outer list
     */
    private final Stack<Integer> olNextIndex = new Stack<>();

    /**
     * @see android.text.Html
     */
    private static void start(final Editable text, final Object mark) {
        final int len = text.length();
        text.setSpan(mark, len, len, Spanned.SPAN_MARK_MARK);
    }

    /**
     * Modified from {@link android.text.Html}
     */
    private static void end(final Editable text, final Class<?> kind, final Object... replaces) {
        final int len = text.length();
        final Object obj = getLast(text, kind);
        final int where = text.getSpanStart(obj);
        text.removeSpan(obj);
        if (where != len) {
            for (final Object replace : replaces) {
                text.setSpan(replace, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * @see android.text.Html
     */
    private static Object getLast(final Spanned text, final Class<?> kind) {
        /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
        final Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        }
        return objs[objs.length - 1];
    }

    @Override
    public void handleTag(
            final boolean opening,
            final String tag,
            final Editable output,
            final XMLReader xmlReader
    ) {
        switch (tag) {
            case "html":
            case "body":
            case "span":
                //nothing to do
                break;
           /* case "code":
                processCode(opening, output);
                break;*/
            case "strike":
            case "s":
                processStrike(opening, output);
                break;
            case "u":
                processU(opening, output);
                break;
            case "ul":
            case "ol":
            case "li":
                processUlOlLi(opening, tag, output);
                break;
            case "img":
                //this must be handled by imageGetter;
                break;
            default:
                Timber.e("Unknown tag in TagHandler with name: %s", tag);
                break;
        }
    }

    /**
     * @link https://bitbucket.org/Kuitsi/android-textview-html-list/src/c866e64acc3336890cfde00fae2e59565fe0c1bf/app/src/main/java/fi/iki/kuitsi/listtest/MyTagHandler.java?at=master&fileviewer=file-view-default
     */
    private void processUlOlLi(final boolean opening, final String tag, final Editable output) {
        if (tag.equalsIgnoreCase("ul")) {
            if (opening) {
                lists.push(tag);
            } else {
                lists.pop();
            }
        } else if (tag.equalsIgnoreCase("ol")) {
            if (opening) {
                lists.push(tag);
                olNextIndex.push(1);//TODO: add support for lists starting other index than 1
            } else {
                lists.pop();
                olNextIndex.pop();
            }
        } else if (tag.equalsIgnoreCase("li")) {
            if (opening) {
                if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                    output.append("\n");
                }
                final String parentList = lists.peek();
                if (parentList.equalsIgnoreCase("ol")) {
                    start(output, new Ol());
                    output.append(olNextIndex.peek().toString()).append(". ");
                    olNextIndex.push(olNextIndex.pop() + 1);
                } else if (parentList.equalsIgnoreCase("ul")) {
                    start(output, new Ul());
                }
            } else {
                if (lists.peek().equalsIgnoreCase("ul")) {
                    if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                        output.append("\n");
                    }
                    // Nested BulletSpans increases distance between bullet and text, so we must prevent it.
                    int bulletMargin = indent;
                    if (lists.size() > 1) {
                        bulletMargin = indent - bullet.getLeadingMargin(true);
                        if (lists.size() > 2) {
                            // This get's more complicated when we add a LeadingMarginSpan into the same line:
                            // we have also counter it's effect to BulletSpan
                            bulletMargin -= (lists.size() - 2) * listItemIndent;
                        }
                    }
                    final BulletSpan newBullet = new BulletSpan(bulletMargin);
                    end(output,
                            Ul.class,
                            new LeadingMarginSpan.Standard(listItemIndent * (lists.size() - 1)),
                            newBullet);
                } else if (lists.peek().equalsIgnoreCase("ol")) {
                    if (output.length() > 0 && output.charAt(output.length() - 1) != '\n') {
                        output.append("\n");
                    }
                    int numberMargin = listItemIndent * (lists.size() - 1);
                    if (lists.size() > 2) {
                        // Same as in ordered lists: counter the effect of nested Spans
                        numberMargin -= (lists.size() - 2) * listItemIndent;
                    }
                    end(output,
                            Ol.class,
                            new LeadingMarginSpan.Standard(numberMargin));
                }
            }
        }
    }

    private static void processStrike(final boolean opening, final Editable output) {
        final int len = output.length();
        if (opening) {
            output.setSpan(new StrikethroughSpan(), len, len, Spanned.SPAN_MARK_MARK);
        } else {
            final Object obj = getLast(output, StrikethroughSpan.class);
            final int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                output.setSpan(new StrikethroughSpan(), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void processU(final boolean opening, final Editable output) {
        Timber.d("processU: %s", output);
        final int len = output.length();
        if (opening) {
            output.setSpan(new UnderlineSpan(), len, len, Spanned.SPAN_MARK_MARK);
        } else {
            final Object obj = getLast(output, UnderlineSpan.class);
            final int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
                output.setSpan(new UnderlineSpan(), where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static class Ul {
    }

    private static class Ol {
    }
}