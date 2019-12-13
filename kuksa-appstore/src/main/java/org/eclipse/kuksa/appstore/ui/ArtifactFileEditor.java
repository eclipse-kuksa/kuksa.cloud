/*******************************************************************************
 * Copyright (C) 2018 Netas Telekomunikasyon A.S. [and others]
 *  
 *  This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *  
 * SPDX-License-Identifier: EPL-2.0
 *  
 * Contributors:
 * Adem Kose, Fatih Ayvaz and Ilker Kuzu (Netas Telekomunikasyon A.S.) - Initial functionality
 * Philipp Heisig (Dortmund University of Applied Sciences and Arts) 
 ******************************************************************************/
package org.eclipse.kuksa.appstore.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.eclipse.kuksa.appstore.client.HawkbitMultiPartFileFeignClient;
import org.eclipse.kuksa.appstore.exception.BadRequestException;
import org.eclipse.kuksa.appstore.client.HawkbitFeignClient;
import org.eclipse.kuksa.appstore.model.hawkbit.Artifact;
import org.eclipse.kuksa.appstore.model.hawkbit.Result;
import org.eclipse.kuksa.appstore.service.AppService;
import org.eclipse.kuksa.appstore.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.ThemeResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.renderers.ImageRenderer;

@SpringComponent
@UIScope
public class ArtifactFileEditor extends VerticalLayout implements View {

	VerticalLayout mainLayout = new VerticalLayout();
	HorizontalLayout vlayout = new HorizontalLayout();
	Grid<Artifact> artifactGrid = new Grid<>(Artifact.class);
	public Upload upload;
	HawkbitFeignClient hawkbitFeignClient;
	HawkbitMultiPartFileFeignClient hawkbitMultiPartFileFeignClient;
	String softwareModuleId;
	private AppService appService;

	@Autowired
	public ArtifactFileEditor(AppService appService) {
		this.appService = appService;

		class ImageUploader implements Receiver, SucceededListener {
			public File file;

			@Override
			public OutputStream receiveUpload(String filename, String mimeType) {

				FileOutputStream fos = null;
				try {

					file = new File(Utils.getImageFilePath() + filename);

					fos = new FileOutputStream(file);

				} catch (final java.io.FileNotFoundException e) {
					new Notification("Could not open file<br/>", e.getMessage(), Notification.Type.ERROR_MESSAGE)
							.show(Page.getCurrent());
					return null;
				}
				return fos;
			}

			@Override
			public void uploadSucceeded(SucceededEvent event) {
				byte[] b = new byte[(int) file.length()];

				try {
					FileInputStream fileInputStream = new FileInputStream(file);

					fileInputStream.read(b);
					try {
						Result<?> result = appService.uploadArtifactWithSoftwareModuleId(softwareModuleId,
								event.getFilename(), b);
						if (result.getStatusCode() == HttpStatus.CREATED) {

							new Notification("The artifact is successfully uploaded!",
									Notification.Type.HUMANIZED_MESSAGE).show(Page.getCurrent());

							fillGrids(softwareModuleId);
						} else {

							new Notification("The uploading process is failed!", result.getErrorMessage(),
									Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (BadRequestException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		;
		ImageUploader receiver = new ImageUploader();
		upload = new Upload(null, receiver);
		upload.setButtonCaption("Click to Upload Artifact File");
		upload.addSucceededListener(receiver);

		vlayout = new HorizontalLayout();
		vlayout.addComponents(artifactGrid);
		mainLayout.addComponent(vlayout);

		vlayout = new HorizontalLayout();
		vlayout.addComponents(createDropPane());
		mainLayout.addComponent(vlayout);

		artifactGrid.setSelectionMode(SelectionMode.NONE);
		artifactGrid.setColumns("id", "providedFilename", "size");
		artifactGrid.setCaption("Artifact Files");
		artifactGrid.getColumn("size").setCaption("Size (B)");
		artifactGrid.getColumn("providedFilename").setCaption("Filename");
		artifactGrid.setWidth(500, Sizeable.Unit.PIXELS);
		artifactGrid.setHeight(300, Sizeable.Unit.PIXELS);

		addDeleteColumn("Delete");

		addComponents(mainLayout);

		setSpacing(true);
	}

	public void setmessageFeignClient(HawkbitFeignClient hawkbitFeignClient) {
		this.hawkbitFeignClient = hawkbitFeignClient;
	}

	public void setHawkbitFeignClient(HawkbitMultiPartFileFeignClient hawkbitMultiPartFileFeignClient) {
		this.hawkbitMultiPartFileFeignClient = hawkbitMultiPartFileFeignClient;
	}

	public final void editArtifactFile(String softwareModuleIdArgument) {
		if (softwareModuleIdArgument == null) {
			setVisible(false);
			return;
		}
		fillGrids(softwareModuleIdArgument);
		softwareModuleId = softwareModuleIdArgument;
	}

	public void fillGrids(String softwareModuleId) {
		try {
			List<Artifact> artifactResultList = hawkbitFeignClient.getArtifactsBysoftwareModuleId(softwareModuleId);

			boolean permissionFileExists = false;
			for (Iterator iterator = artifactResultList.iterator(); iterator.hasNext();) {
				Artifact artifact = (Artifact) iterator.next();
				if (artifact.getProvidedFilename().equals(Utils.PERMISSION)) {
					permissionFileExists = true;
					break;
				}

			}
			if (permissionFileExists == false) {
				new Notification("There is no " + Utils.PERMISSION + " file.",
						" You should add " + Utils.PERMISSION
								+ " file to specify permissions that are used on this application!",
						Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			}

			artifactGrid.setItems(artifactResultList);
		} catch (Exception e) {
			new Notification("Hawkbit connection error. Check your Hawkbit's IP in the propreties file!",
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		}
	}

	private void addDeleteColumn(String caption) {
		ImageRenderer<Artifact> renderer = new ImageRenderer<>();
		renderer.addClickListener(e -> {
			try {
				deleteArtifact(e.getItem());
			} catch (NumberFormatException e2) {
				e2.printStackTrace();
			} catch (BadRequestException e2) {
				e2.printStackTrace();
			}
		});

		Grid.Column<Artifact, ThemeResource> iconColumn = artifactGrid
				.addColumn(i -> new ThemeResource("img/delete.png"), renderer);
		iconColumn.setCaption(caption);
		iconColumn.setMaximumWidth(100);
		artifactGrid.addItemClickListener(e -> {
			if (e.getColumn().equals(iconColumn)) {
				try {
					deleteArtifact(e.getItem());
				} catch (NumberFormatException e1) {
					e1.printStackTrace();
				} catch (BadRequestException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private void deleteArtifact(Artifact artifact) throws NumberFormatException, BadRequestException {
		Result<?> result = appService.deleteArtifactWithSoftwareModuleId(softwareModuleId, artifact.getId());
		if (result.getStatusCode() == HttpStatus.OK) {
			new Notification("The artifact is successfully deleted.", Notification.Type.HUMANIZED_MESSAGE)
					.show(Page.getCurrent());
			fillGrids(softwareModuleId);
		} else {
			new Notification("The deleting process is failed.", Notification.Type.ERROR_MESSAGE)
					.show(Page.getCurrent());
		}
	}

	private VerticalLayout createDropPane() {
		VerticalLayout dropPane;

		Image imageDragAndDrop = new Image();
		imageDragAndDrop.setWidth("40");
		imageDragAndDrop.setHeight("40");
		imageDragAndDrop.setSource(new ThemeResource("img" + "/" + "dragandrop.png"));

		dropPane = new VerticalLayout(imageDragAndDrop, new Label("Drop Artifact File to upload"), new Label("or"),
				upload);
		dropPane.addStyleName("drop-area");
		dropPane.setWidth(500, Sizeable.Unit.PIXELS);

		ProgressBar progress = new ProgressBar();
		progress.setIndeterminate(true);
		progress.setVisible(false);
		dropPane.addComponent(progress);

		new FileDropTarget<>(dropPane, fileDropEvent -> {
			final int fileSizeLimit = 200 * 1024 * 1024; // 200MB

			fileDropEvent.getFiles().forEach(html5File -> {
				final String fileName = html5File.getFileName();

				if (html5File.getFileSize() > fileSizeLimit) {
					Notification.show("File rejected. Max 200MB files are accepted by Sampler",
							Notification.Type.WARNING_MESSAGE);
				} else {
					final ByteArrayOutputStream bas = new ByteArrayOutputStream();
					final StreamVariable streamVariable = new StreamVariable() {

						@Override
						public OutputStream getOutputStream() {
							return bas;
						}

						@Override
						public boolean listenProgress() {
							return false;
						}

						@Override
						public void onProgress(final StreamingProgressEvent event) {
						}

						@Override
						public void streamingStarted(final StreamingStartEvent event) {
						}

						@Override
						public void streamingFinished(final StreamingEndEvent event) {
							progress.setVisible(false);
							try {
								appService.uploadArtifactWithSoftwareModuleId(softwareModuleId, fileName,
										bas.toByteArray());
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (BadRequestException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void streamingFailed(final StreamingErrorEvent event) {
							progress.setVisible(false);
						}

						@Override
						public boolean isInterrupted() {
							return false;
						}
					};
					html5File.setStreamVariable(streamVariable);
					progress.setVisible(true);
				}
			});
		});
		return dropPane;
	}
}
