/******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: JBoss by Red Hat - Initial implementation.
 *****************************************************************************/
package org.jboss.tools.fuse.transformation.editor;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.text.StringCharacterIterator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.util.SafeRunnable;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TransformationEditorIT {

	private static final String POM_START = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"" +
			"		  xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
			"    	  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
			"  <modelVersion>4.0.0</modelVersion>\n" +
			"  <groupId>com.mycompany</groupId>\n" +
			"  <artifactId>testproject</artifactId>\n" +
			"  <version>1.0.0-SNAPSHOT</version>\n" +
			"  <packaging>bundle</packaging>\n" +
			"  <name>Some Dummy Project</name>\n" +
			"  <build>\n" +
			"    <plugins>\n";

	private static final String POM_END = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"    </plugins>\n" +
			"  </build>\n" +
			"</project>";

	private static final String EXPECTED_POM = POM_START +
			"      <plugin>\n" +
			"		 <groupId>org.apache.felix</groupId>\n" +
			"        <artifactId>maven-bundle-plugin</artifactId>\n" +
			"        <version>3.2.0</version>\n" +
			"		 <extensions>true</extensions>\n" +
			"        <configuration>\n" +
			"          <instructions>\n" +
			"		     <Import-Package>*,com.sun.el;version=\"[2,3)\";resolution:=optional</Import-Package>\n" +
			"		   </instructions>\n" +
			"		   <archive>\n" +
			"		 	 <Project-Group-Id>${project.groupId}</Project-Group-Id>\n" +
			"		 	 <Project-Artifact-Id>${project.artifactId}</Project-Artifact-Id>\n" +
			"		 	 <Project-Version>${project.version}</Project-Version>\n" +
			"		   </archive>\n" +
			"        </configuration>\n" +
			"      </plugin>\n" +
			POM_END;

	private static final String POM_WITHOUT_PLUGIN = POM_START + POM_END;

	private static final String POM_WITHOUT_CONFIGURATION = POM_START +
			"      <plugin>\n" +
			"		 <groupId>org.apache.felix</groupId>\n" +
			"        <artifactId>maven-bundle-plugin</artifactId>\n" +
			"        <version>3.2.0</version>\n" +
			"		 <extensions>true</extensions>\n" +
			"      </plugin>\n" +
			POM_END;

	private static final String POM_WITHOUT_INSTRUCTIONS = POM_START +
			"      <plugin>\n" +
			"		 <groupId>org.apache.felix</groupId>\n" +
			"        <artifactId>maven-bundle-plugin</artifactId>\n" +
			"        <version>3.2.0</version>\n" +
			"		 <extensions>true</extensions>\n" +
			"        <configuration>\n" +
			"		   <archive>\n" +
			"		 	 <Project-Group-Id>${project.groupId}</Project-Group-Id>\n" +
			"		 	 <Project-Artifact-Id>${project.artifactId}</Project-Artifact-Id>\n" +
			"		 	 <Project-Version>${project.version}</Project-Version>\n" +
			"		   </archive>\n" +
			"        </configuration>\n" +
			"      </plugin>\n" +
			POM_END;

	private static final String POM_WITHOUT_IMPORT_PACKAGE = POM_START +
			"      <plugin>\n" +
			"		 <groupId>org.apache.felix</groupId>\n" +
			"        <artifactId>maven-bundle-plugin</artifactId>\n" +
			"        <version>3.2.0</version>\n" +
			"		 <extensions>true</extensions>\n" +
			"        <configuration>\n" +
			"          <instructions>\n" +
			"		   </instructions>\n" +
			"		   <archive>\n" +
			"		 	 <Project-Group-Id>${project.groupId}</Project-Group-Id>\n" +
			"		 	 <Project-Artifact-Id>${project.artifactId}</Project-Artifact-Id>\n" +
			"		 	 <Project-Version>${project.version}</Project-Version>\n" +
			"		   </archive>\n" +
			"        </configuration>\n" +
			"      </plugin>\n" +
			POM_END;

	@Rule
	public FuseProject fuseProject = new FuseProject(TransformationEditorIT.class.getName());

	private boolean safeRunnableIgnoreErrorStateBeforeTests;
	private TransformationEditor editor = new TransformationEditor();
	private IProject project = fuseProject.project;
	private IFile pomIFile = project.getFile("pom.xml");
	private IProgressMonitor monitor = new NullProgressMonitor();
	private File pomFile = project.getLocation().append("pom.xml").toFile();

	@Before
	public void setup() throws Exception {
		safeRunnableIgnoreErrorStateBeforeTests = SafeRunnable.getIgnoreErrors();
		SafeRunnable.setIgnoreErrors(false);
		pomIFile.delete(true, monitor);
	}

	@After
	public void tearDown() {
		SafeRunnable.setIgnoreErrors(safeRunnableIgnoreErrorStateBeforeTests);
	}

	@Test
	public void shouldUpdatePomWithoutPlugin() throws Exception {
		updatePom(POM_WITHOUT_PLUGIN);
	}

	@Test
	public void shouldUpdatePomWithoutConfiguration() throws Exception {
		updatePom(POM_WITHOUT_CONFIGURATION);
	}

	@Test
	public void shouldUpdatePomWithoutInstructions() throws Exception {
		updatePom(POM_WITHOUT_INSTRUCTIONS);
	}

	@Test
	public void shouldUpdatePomWithoutImportPackage() throws Exception {
		updatePom(POM_WITHOUT_IMPORT_PACKAGE);
	}

	@Test
	public void shouldNotModifyExpectedPom() throws Exception {
		updatePom(EXPECTED_POM);
	}

	private void updatePom(String pom) throws Exception {
		pomIFile.create(new ByteArrayInputStream(pom.getBytes()), true, monitor);
		editor.updateManifestPackageImports(project, pomFile);
		char[] buf = new char[EXPECTED_POM.length()];
		try (InputStreamReader reader = new InputStreamReader(pomIFile.getContents())) {
			reader.read(buf);
		}
		assertThat(normalize(String.valueOf(buf))).isEqualTo(normalize(EXPECTED_POM));
	}

	private String normalize(String text) {
		StringBuilder builder = new StringBuilder();
		StringCharacterIterator iter = new StringCharacterIterator(text);
		for (char chr = iter.first(); chr != StringCharacterIterator.DONE; chr = iter.next()) {
			if (!Character.isWhitespace(chr)) builder.append(chr);
		}
		return builder.toString();
	}
}
