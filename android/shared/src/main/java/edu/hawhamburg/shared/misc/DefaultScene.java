/**
 * Diese Datei gehört zum Android/Java Framework zur Veranstaltung "Computergrafik für
 * Augmented Reality" von Prof. Dr. Philipp Jenke an der Hochschule für Angewandte
 * Wissenschaften (HAW) Hamburg. Weder Teile der Software noch das Framework als Ganzes dürfen
 * ohne die Einwilligung von Philipp Jenke außerhalb von Forschungs- und Lehrprojekten an der HAW
 * Hamburg verwendet werden.
 * <p>
 * This file is part of the Android/Java framework for the course "Computer graphics for augmented
 * reality" by Prof. Dr. Philipp Jenke at the University of Applied (UAS) Sciences Hamburg. Neither
 * parts of the framework nor the complete framework may be used outside of research or student
 * projects at the UAS Hamburg.
 */
package edu.hawhamburg.shared.misc;

import android.util.Log;

import java.util.List;

import edu.hawhamburg.shared.datastructures.mesh.ITriangleMesh;
import edu.hawhamburg.shared.datastructures.mesh.ObjReader;
import edu.hawhamburg.shared.datastructures.mesh.TriangleMeshTools;
import edu.hawhamburg.shared.scenegraph.INode;
import edu.hawhamburg.shared.scenegraph.InnerNode;
import edu.hawhamburg.shared.scenegraph.ScaleNode;
import edu.hawhamburg.shared.scenegraph.TriangleMeshNode;

/**
 * Dummy scene with rather simple content.
 *
 * @author Philipp Jenke
 */
public class DefaultScene extends Scene {


    public DefaultScene() {
        super(100, INode.RenderMode.REGULAR);
    }

    @Override
    public void onSetup(InnerNode rootNode) {

        Button button = new Button("kanone_abfeuern.png",
                -0.7, -0.7, 0.2, new ButtonHandler() {
            @Override
            public void handle() {
                Log.i(Constants.LOGTAG, "Button 1 pressed!");
            }
        });
        //addButton(button);


        // Airplane
        ScaleNode objectNode = new ScaleNode(1);
        ObjReader reader = new ObjReader();
        List<ITriangleMesh> meshes = reader.read("meshes/plane.obj");
        TriangleMeshTools.fitToUnitBox(meshes);
        TriangleMeshTools.placeOnXZPlane(meshes);
        for (ITriangleMesh mesh : meshes) {
            objectNode.addChild(new TriangleMeshNode(mesh));
        }
        rootNode.addChild(objectNode);
    }

    @Override
    public void onTimerTick(int counter) {
        // Timer tick event
    }

    @Override
    public void onSceneRedraw() {

    }
}
