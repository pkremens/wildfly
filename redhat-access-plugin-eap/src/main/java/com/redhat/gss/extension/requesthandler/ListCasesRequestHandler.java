/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.redhat.gss.extension.requesthandler;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import com.redhat.gss.extension.RedhatAccessPluginEapDescriptions;
import com.redhat.gss.extension.RedhatAccessPluginEapExtension;
import com.redhat.gss.redhat_support_lib.api.API;
import com.redhat.gss.redhat_support_lib.infrastructure.Comments;
import com.redhat.gss.redhat_support_lib.parsers.Case;
import com.redhat.gss.redhat_support_lib.parsers.Comment;
import com.redhat.gss.redhat_support_lib.parsers.Product;
import com.redhat.gss.redhat_support_lib.parsers.Solution;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;

public class ListCasesRequestHandler extends BaseRequestHandler implements
		OperationStepHandler, DescriptionProvider {

	public static final String OPERATION_NAME = "list-cases";
	public static final ListCasesRequestHandler INSTANCE = new ListCasesRequestHandler();

	public ListCasesRequestHandler() {
		super(PathElement.pathElement(OPERATION_NAME), RedhatAccessPluginEapExtension
				.getResourceDescriptionResolver(OPERATION_NAME), INSTANCE,
				INSTANCE, OPERATION_NAME);
	}

	@Override
	public void execute(OperationContext context, ModelNode operation)
			throws OperationFailedException {
		// In MODEL stage, just validate the request. Unnecessary if the request
		// has no parameters
		validator.validate(operation);
		context.addStep(new OperationStepHandler() {

			@Override
			public void execute(OperationContext context, ModelNode operation)
					throws OperationFailedException {
				API api = null;
				try {
					api = getAPI(context, operation);
				} catch (MalformedURLException e) {
					throw new OperationFailedException(e.getLocalizedMessage(),
							e);
				}
				List<Case> cases = null;
				try {
					cases = api.getCases().list(null, false, true, null, null,
							null, null, null, null);
				} catch (Exception e) {
					throw new OperationFailedException(e.getLocalizedMessage(),
							e);
				}
				ModelNode response = context.getResult();
				int i = 0;
				for (Case cas : cases) {
					if (cas.getCaseNumber() != null) {
						ModelNode caseNode = response.get(i);
						caseNode.get("Case").set(cas.getCaseNumber());
						if (cas.getSummary() != null) {
							caseNode.get("Summary").set(cas.getSummary());
						}
						if (cas.getType() != null) {
							caseNode.get("Case Type").set(cas.getType());
						}
						if (cas.getSeverity() != null) {
							caseNode.get("Severity").set(cas.getSeverity());
						}
						if (cas.getStatus() != null) {
							caseNode.get("Status").set(cas.getStatus());
						}
						if (cas.getAlternateId() != null) {
							caseNode.get("Alternate Id").set(
									cas.getAlternateId());
						}
						if (cas.getProduct() != null) {
							caseNode.get("Product").set(cas.getProduct());
						}
						if (cas.getOwner() != null) {
							caseNode.get("Owner").set(cas.getOwner());
						}
						// if(cas.getContactName() != null){
						// caseNode.get("Red Hat Owner").set(cas.getCon);
						// }
						// if(cas.get!= null){
						// caseNode.get("Account Name").set(cas.getOwner());
						// }
						if (cas.getCreatedDate() != null) {
							caseNode.get("Opened").set(
									cas.getCreatedDate().getTime().toString());
						}
						if (cas.getLastModifiedDate() != null) {
							caseNode.get("Last Updated").set(
									cas.getLastModifiedDate().getTime().toString());
						}
						if (cas.getAccountNumber() != null) {
							caseNode.get("Account Number").set(
									cas.getAccountNumber());
						}
						if (cas.getDescription() != null) {
							caseNode.get("Description").set(
									cas.getDescription());
						}

//						if (cas.getComments() != null) {
//							int j = 0;
//							for (Comment comment : cas.getComments()
//									.getComment()) {
//								if (comment.getId() != null) {
//									ModelNode commentNode = caseNode.get(j)
//											.set("Comment: " + comment.getId());
//									if (comment.getCreatedBy() != null) {
//										commentNode.get("Author").set(
//												comment.getCreatedBy());
//									}
//									if (comment.getCreatedDate() != null) {
//										commentNode.get("Date").set(
//												comment.getCreatedDate()
//														.toString());
//									}
//									if (comment.getText() != null) {
//										commentNode.get("Text").set(
//												comment.getText());
//									}
//								}
//							}
//						}
					}
				}

				context.completeStep();
			}
		}, OperationContext.Stage.RUNTIME);

		context.completeStep();
	}

	@Override
	public ModelNode getModelDescription(Locale locale) {
		return RedhatAccessPluginEapDescriptions.getRedhatAccessPluginEapRequestDescription(locale);
	}
}
