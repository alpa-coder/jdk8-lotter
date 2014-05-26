package be.jacobsvanroy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Copyright (C) 2014  Davy Van Roy
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class Lotter {

    private final LotterDraws lotterDraws;
    private final LotterDraw lotterDraw;

    public Lotter() {
        this.lotterDraws = new LotterDraws();
        this.lotterDraw = new LotterDraw();
    }

    public static void main(String... args) {
        Lotter lotter = new Lotter();
        List<Integer> allNumbers = lotter.getAllNumbers();
        System.out.println(allNumbers);
        List<Map.Entry<Integer, Long>> statistics = lotter.printStatistics(allNumbers);
        System.out.println(statistics);
    }

    public List<Map.Entry<Integer, Long>> printStatistics(List<Integer> allNumbers) {
        return
                allNumbers
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        x -> x,
                                        Collectors.counting()
                                )
                        )
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toList());
    }

    private List<Integer> getAllNumbers() {
        return
                lotterDraws.getDraws()
                        .stream()
                        .parallel()
                        .limit(10)
                        .map(lotterDraw::getDraw)
                        .collect(
                                ArrayList::new,
                                (left, right) -> left.addAll(right),
                                (left, right) -> left.addAll(right)
                        );
    }


}
