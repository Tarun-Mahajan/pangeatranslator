package pangea.graphdrawing;

import pangea.pnml.type.*;
import pangea.pnml.type.Graphics;
import pangea.logging.Log;
import org.jgraph.graph.*;
import org.jgraph.JGraph;

import java.util.List;
import java.util.Hashtable;
import java.util.Map;
import java.awt.geom.Rectangle2D;
import java.awt.*;

import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.JGraphFacade;

import javax.swing.*;

public class Draw {

    private static final int XLABELOFFSET = 200;
    private static final int YLABELOFFSET = 80;


    public static JFrame draw(Pnml pnmlGraph, boolean out_frame) throws Exception {
        GraphModel model = new DefaultGraphModel();
        GraphLayoutCache view = new GraphLayoutCache(model, new DefaultCellViewFactory());
        JGraph graph = new JGraph(model, view);

        List<Place> places = pnmlGraph.getPnml().getPlaces();
        List<Transition> transitions = pnmlGraph.getPnml().getTransitions();
        List<Arc> arcs = pnmlGraph.getPnml().getArcs();

        Hashtable<String, Integer> htplaces = new Hashtable<String, Integer>(places.size());
        Hashtable<String, Integer> httransitions = new Hashtable<String, Integer>(transitions.size());

        DefaultGraphCell[] cells = new DefaultGraphCell[places.size() + transitions.size() + arcs.size()];

        int cpos = 0;

        //places->positions setup
        for (Place place : places) {
            cells[cpos] = new DefaultGraphCell(place);
            GraphConstants.setBounds(cells[cpos].getAttributes(), new Rectangle2D.Double(0, 0, 40, 20));
            GraphConstants.setGradientColor(cells[cpos].getAttributes(), Color.orange);
            GraphConstants.setOpaque(cells[cpos].getAttributes(), true);
            cells[cpos].addPort();

            htplaces.put(place.getId(), cpos);

            cpos++;
        }

        //transitions->positions setup
        for (Transition transition : transitions) {
            cells[cpos] = new DefaultGraphCell(transition);
            GraphConstants.setBounds(cells[cpos].getAttributes(), new Rectangle2D.Double(0, 0, 40, 20));
            GraphConstants.setGradientColor(cells[cpos].getAttributes(), Color.gray);
            GraphConstants.setOpaque(cells[cpos].getAttributes(), true);
            cells[cpos].addPort();

            httransitions.put(transition.getId(), cpos);

            cpos++;
        }

        //arcs setup
        for (Arc arc : arcs) {
            DefaultEdge edge = new DefaultEdge(arc.getInscription().getValue());
            Integer spos = htplaces.get(arc.getSource());
            Integer tpos;
            if (spos == null) {
                spos = httransitions.get(arc.getSource());
                tpos = htplaces.get(arc.getTarget());
            } else {
                tpos = httransitions.get(arc.getTarget());
            }
            edge.setSource(cells[spos].getChildAt(0));
            edge.setTarget(cells[tpos].getChildAt(0));
            GraphConstants.setLineEnd(edge.getAttributes(), GraphConstants.ARROW_CLASSIC);
            GraphConstants.setEndFill(edge.getAttributes(), true);

            cells[cpos] = edge;
            cpos++;
        }

        //add elements to graph
        graph.getGraphLayoutCache().insert(cells);

        //graph redrawing
        JGraphFacade facade = new JGraphFacade(graph);
        JGraphLayout layout = new JGraphFastOrganicLayout();
        layout.run(facade);
        Map nested = facade.createNestedMap(true, true);
        graph.getGraphLayoutCache().edit(nested);


        //setting draw values into the pnml file
        int ppos = 0;
        int tpos = 0;
        boolean found = false;
        for (int i = 0; i < cells.length && !found; i++) {
            DefaultGraphCell cell = cells[i];
            Rectangle2D rec = GraphConstants.getBounds(cell.getAttributes());
            if (cell.getUserObject() instanceof Place) {
                Place obj = places.get(ppos++);
                Graphics g = new Graphics();
                Graphics.GraphicsInner gi = new Graphics.GraphicsInner();
                Position pos = new Position();
                pos.setX(new Long(Math.round(rec.getX())).intValue() + XLABELOFFSET);
                pos.setY(new Long(Math.round(rec.getY())).intValue() + YLABELOFFSET);
                gi.setPosition(pos);
                g.setGraphics(gi);
                obj.setGraphics(g);
            } else if (cell.getUserObject() instanceof Transition) {
                Transition obj = transitions.get(tpos++);
                Graphics g = new Graphics();
                Graphics.GraphicsInner gi = new Graphics.GraphicsInner();
                Position pos = new Position();
                pos.setX(new Long(Math.round(rec.getX())).intValue() + XLABELOFFSET);
                pos.setY(new Long(Math.round(rec.getY())).intValue() + YLABELOFFSET);
                gi.setPosition(pos);
                g.setGraphics(gi);
                obj.setGraphics(g);
            } else {
                found=true;
            }
        }
        Log.newMessage(pnmlGraph.getPnml().getId() + " redrawing complete.");

        if (out_frame) {
            JFrame frame = new JFrame();
            frame.getContentPane().add(new JScrollPane(graph));
            frame.pack();
            return frame;
        }

        return null;
    }
}