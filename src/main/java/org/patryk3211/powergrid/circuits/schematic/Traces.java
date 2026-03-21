package org.patryk3211.powergrid.circuits.schematic;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Traces {
    private List<List<Line>> verticals;
    private List<List<Line>> horizontals;

    public Traces(int gridSize) {
        verticals = new ArrayList<>(gridSize);
        horizontals = new ArrayList<>(gridSize);
        for (int i = 0; i < gridSize; i++) {
            verticals.add(new ArrayList<>());
            horizontals.add(new ArrayList<>());
        }
    }

    /**
     * Copy constructor performs a clone of other on the assumption that underlying Line objects are immutable
     * and don't need to be cloned
     * @param other the Lines object to clone
     */
    public Traces(Traces other) {
        verticals = cloneLineList(other.verticals);
        horizontals = cloneLineList(other.horizontals);
    }

    /**
     * Determine whether two positive inclusive ranges overlap
     * @return true if [start1, end1] and [start2, end2] overlap
     */
    private static boolean rangesOverlap(int start1, int end1, int start2, int end2) {
        if (start1 > end1 || start2 > end2) {
            throw new IllegalArgumentException("One of the ranges was negative: " + start1 + ".." + end1 + ", " + start2 + ".." + end2);
        }
        return start1 <= end2 && start2 <= end1;
    }

    public void addTrace(boolean vertical, int position, int start, int end) {
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        List<Line> channel = (vertical ? verticals : horizontals).get(position);
        List<Line> overlaps = new ArrayList<>();
        int newStart = start;
        int newEnd = end;
        for (var line : channel) {
            if (rangesOverlap(start, end, line.start(), line.end())) {
                overlaps.add(line);
                newStart = Math.min(newStart, line.start());
                newEnd = Math.max(newEnd, line.end());
            }
        }
        channel.removeAll(overlaps);
        channel.add(new Line(vertical, position, newStart, newEnd));
    }

    public void clear(int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }

        // TODO: horizontals
        for (int position = x1; position <= x2; position++) {
            List<Line> channel = verticals.get(position);
            List<Line> toShorten = new ArrayList<>();
            for (Line line : channel) {
                if (rangesOverlap(y1, y2, line.start(), line.end())) {
                    toShorten.add(line);
                }
            }

            channel.removeAll(toShorten);

            for (Line line : toShorten) {
                channel.add(new Line(
                        line.vertical(), line.position(),
                        Math.max(line.start(), y1), Math.min(line.end(), y2)
                ));
            }
        }
    }

//    private Traces(List<List<Line>> lines) {
//        this.lines = lines;
//    }

//    /**
//     * Gets a read-only view of this Lines
//     * @return a read-only view of this Lines
//     */
//    public Traces readOnly() {
//        return new Traces(Collections.unmodifiableList(lines));
//    }

//    public List<Line> getVerticalLines() {
//        List<Line> flat = new ArrayList<>();
//        verticals.forEach(flat::addAll);
//        return flat;
//    }

    public TraceIterable iterateVerticals() {
        return iterateOverRange(verticals, 0, verticals.size() - 1);
    }

    public TraceIterable iterateVerticals(int start, int end) {
        return iterateOverRange(verticals, start, end);
    }

    public TraceIterable iterateHorizontals() {
        return iterateOverRange(horizontals, 0, horizontals.size() - 1);
    }

    public TraceIterable iterateHorizontals(int start, int end) {
        return iterateOverRange(horizontals, start, end);
    }

    private TraceIterable iterateOverRange(List<List<Line>> lines, int start, int end) {
        if (start < 0 || end < 0) {
            throw new IndexOutOfBoundsException("One of the bounds " + start + ", " + end + " were negative");
        }
        else if (start >= lines.size() || end >= lines.size()) {
            throw new IndexOutOfBoundsException("One of the bounds " + start + ", " + end + " was greater than grid size (" + lines.size() + ")");
        }
        else if (start > end) {
            throw new IndexOutOfBoundsException("Starting bound " + start + " > ending bound " + end);
        }

        return new TraceIterable(lines, start, end);
    }

    public class TraceIterable implements Iterable<Line> {
        private final List<List<Line>> lines;
        private final int start;
        private final int end;

        private TraceIterable(List<List<Line>> lines, int start, int end) {
            this.lines = lines;
            this.start = start;
            this.end = end;
        }

        @Override
        public @NotNull Iterator<Line> iterator() {
            return new Iterator<>() {
                int i = start;
                int j = 0;

                private boolean inLegalSublist() {
                    return i < end && i < lines.size();
                }

                private void getToLegalValue() {
                    while (inLegalSublist() && j >= lines.get(i).size()) {
                        j = 0;
                        i++;
                    }
                }

                @Override
                public boolean hasNext() {
                    getToLegalValue();
                    return inLegalSublist();
                }

                @Override
                public Line next() throws NoSuchElementException {
                    getToLegalValue();
                    if (!inLegalSublist()) {
                        throw new NoSuchElementException();
                    }
                    return lines.get(i).get(j++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

//    @Override
//    public @NotNull Iterator<Line> iterator() {
//        return iterateOverRange(0, lines.size() - 1).iterator();
//    }

    private static List<List<Line>> cloneLineList(List<List<Line>> list) {
        List<List<Line>> result = new ArrayList<>(list.size());
        for (List<Line> srcSublist : list) {
            List<Line> dstSublist = new ArrayList<>(srcSublist.size());
            dstSublist.addAll(srcSublist);
            result.add(dstSublist);
        }
        return result;
    }
}

