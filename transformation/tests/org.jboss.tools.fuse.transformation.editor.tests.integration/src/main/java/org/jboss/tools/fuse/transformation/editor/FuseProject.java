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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.rules.ExternalResource;

public class FuseProject extends ExternalResource {

	private String projectName;
	IProject project;

	public FuseProject(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.junit.rules.ExternalResource#before()
	 */
	@Override
	protected void before() throws Throwable {
		super.before();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists()) project.create(null);
		if (!project.isOpen()) project.open(null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.junit.rules.ExternalResource#after()
	 */
	@Override
	protected void after() {
		super.after();
		if (project != null && project.exists()) {
			try {
				project.delete(true, new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
