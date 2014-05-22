package be.jacobsvanroy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class LotterDraw {

    private static final String LOTTER_DRAW_URL = "http://www.thelotter.com/lottery-results/usa-megamillions?DrawNumber=";

    public List<Integer> getDraw(int drawNo) {
        try {
            Document doc = Jsoup.connect(LOTTER_DRAW_URL + drawNo).get();
            Elements balls = doc.select(".ballTexticon");
            List<Integer> draws = new ArrayList<>();
            balls.forEach(b -> draws.add(Integer.parseInt(b.text())));
            return draws;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
