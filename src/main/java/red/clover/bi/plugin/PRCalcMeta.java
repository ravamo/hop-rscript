/*
 *   This file is part of PRCalcPlugin.
 *
 *   PRCalcPlugin is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PRCalcPlugin is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PPRCalcPlugin.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2013 dekarlab.de Theory. Solutions. Online Training.
 */

package red.clover.bi.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.Const;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.ITransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.w3c.dom.Node;

import red.clover.bi.PRVariable;

/**
 * Calc R step.
 * 
 */

@Transform(
		id = "SampleTransform",
		name = "R Script Executor",
		description = "Executes a R script",
		image = "R-logo.svg",
		categoryDescription = "Statistics")
public class PRCalcMeta extends BaseTransformMeta<PRCalc,PRCalcData> {

	private String scriptFilePath;
	private List<PRVariable> inputVars;
	private List<PRVariable> outputVars;
	private boolean inputTable;

	/**
	 * Constructor.
	 */
	public PRCalcMeta() {
		super();
	}



	/**
	 * Load XML.
	 */
//	public final void loadXML(final Node stepnode, //todo swap new loadXml method by hop hierarchy
//			final List<DatabaseMeta> databases,
//			final Map<String, Counter> counters) throws HopException {
//		readData(stepnode, databases);
//	}


	@Override // todo swap new loadXml method by hop hierarchy
	public void loadXml(final Node transformNode, final IHopMetadataProvider metadataProvider) throws HopXmlException {
		try {
			readData(transformNode, metadataProvider);
		} catch (HopException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clone MetaData.
	 * 
	 * @return
	 */
	public final Object clone() {
		PRCalcMeta retval = (PRCalcMeta) super.clone();
		return retval;
	}

	/**
	 * Read meta data from XML.
	 * 
	 * @param stepnode
	 * @param databases
	 * @throws HopException
	 */
	private void readData(final Node stepnode,
			final IHopMetadataProvider databases)
			throws HopException {
		try {
			scriptFilePath = XmlHandler.getTagValue(stepnode, "scriptfilepath");
			inputTable = false;
			String value = XmlHandler.getTagValue(stepnode, "inputTable");
			if (value != null && value.equals("true")) {
				inputTable = true;
			}
			this.inputVars = new ArrayList<PRVariable>();
			this.outputVars = new ArrayList<PRVariable>();

			Node infields = XmlHandler.getSubNode(stepnode, "inputvars");
			int nrInFields = XmlHandler.countNodes(infields, "inputvar");
			for (int i = 0; i < nrInFields; i++) {
				Node fnode = XmlHandler.getSubNodeByNr(infields, "inputvar", i);
				String rName = XmlHandler.getTagValue(fnode, "rname");
				String pentahoName = XmlHandler.getTagValue(fnode,
						"pentahoname");
				int type = Integer.parseInt(XmlHandler.getTagValue(fnode,
						"type"));
				inputVars.add(new PRVariable(rName, pentahoName, type));
			}

			Node outfields = XmlHandler.getSubNode(stepnode, "outputvars");
			int nrOutFields = XmlHandler.countNodes(outfields, "outputvar");
			for (int i = 0; i < nrOutFields; i++) {
				Node fnode = XmlHandler.getSubNodeByNr(outfields, "outputvar",
						i);
				String rName = XmlHandler.getTagValue(fnode, "rname");
				String pentahoName = XmlHandler.getTagValue(fnode,
						"pentahoname");
				int type = Integer.parseInt(XmlHandler.getTagValue(fnode,
						"type"));
				outputVars.add(new PRVariable(rName, pentahoName, type));
			}
		} catch (Exception e) {
			throw new HopException("Unable to load step info from XML", e);
		}
	}

	@Override
	public String getXml() throws HopException {

		StringBuilder retval = new StringBuilder();
		retval.append("    ").append(
			XmlHandler.addTagValue("scriptfilepath", scriptFilePath));
		retval.append("    ").append(
			XmlHandler.addTagValue("inputTable", (inputTable ? "true"
				: "false")));
		retval.append("    <inputvars>").append(Const.CR);
		for (PRVariable field : inputVars) {
			retval.append("      <inputvar>").append(Const.CR);
			retval.append("        ").append(
				XmlHandler.addTagValue("rname", field.getrName()));
			retval.append("        ").append(
				XmlHandler.addTagValue("pentahoname",
					field.getPentahoName()));
			retval.append("        ").append(
				XmlHandler.addTagValue("type", field.getType()));
			retval.append("      </inputvar>").append(Const.CR);
		}
		retval.append("    </inputvars>").append(Const.CR);

		retval.append("    <outputvars>").append(Const.CR);
		for (PRVariable field : outputVars) {
			retval.append("      <outputvar>").append(Const.CR);
			retval.append("        ").append(
				XmlHandler.addTagValue("rname", field.getrName()));
			retval.append("        ").append(
				XmlHandler.addTagValue("pentahoname",
					field.getPentahoName()));
			retval.append("        ").append(
				XmlHandler.addTagValue("type", field.getType()));
			retval.append("      </outputvar>").append(Const.CR);
		}
		retval.append("    </outputvars>").append(Const.CR);

		return retval.toString();
	}





	@Override // todo migrate getFields
	public void getFields(final IRowMeta inputRowMeta, final String name, final IRowMeta[] info, final TransformMeta nextTransform, final IVariables variables,
		final IHopMetadataProvider metadataProvider) throws HopTransformException {

		for (PRVariable outputVar : outputVars) {
			IValueMeta v = new ValueMetaBase(outputVar
				.getPentahoName(),
				PRCalc.convertRTypeToPentahoType(outputVar
					.getType()));
			v.setOrigin(name);
			inputRowMeta.addValueMeta(v);
		}

	}



	@Override //todo migrate check
	public void check(final List<ICheckResult> remarks, final PipelineMeta pipelineMeta, final TransformMeta transformMeta, final IRowMeta prev,
		final String[] input, final String[] output,
		final IRowMeta info, final IVariables variables, final IHopMetadataProvider metadataProvider) {

		CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
			"OK", transformMeta);
		remarks.add(cr);

	}




	@Override // todo migrate getDialog
	public String getDialogClassName() {
		return PRCalcDialog.class.getCanonicalName();
	}


	/**
	 * Get used DB connections.
	 */
	@Override
	public final DatabaseMeta[] getUsedDatabaseConnections() {
		return super.getUsedDatabaseConnections();
	}

	/**
	 * Set default values.
	 */
	@Override
	public void setDefault() {
		scriptFilePath = "file.r";
		inputVars = new ArrayList<PRVariable>();
		outputVars = new ArrayList<PRVariable>();
	}

	/**
	 * Get script file path.
	 * 
	 * @return
	 */
	public String getScriptFilePath() {
		return scriptFilePath;
	}

	/**
	 * Set script file path.
	 * 
	 * @param scriptFilePath
	 */
	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}

	/**
	 * Get input vars.
	 * 
	 * @return
	 */
	public List<PRVariable> getInputVars() {
		return inputVars;
	}

	/**
	 * Set input vars.
	 * 
	 * @param inputVars
	 */
	public void setInputVars(List<PRVariable> inputVars) {
		this.inputVars = inputVars;
	}

	/**
	 * Get output vars.
	 * 
	 * @return
	 */
	public List<PRVariable> getOutputVars() {
		return outputVars;
	}

	/**
	 * Set output vars.
	 * 
	 * @param outputVars
	 */
	public void setOutputVars(List<PRVariable> outputVars) {
		this.outputVars = outputVars;
	}

	public boolean isInputTable() {
		return inputTable;
	}

	public void setInputTable(boolean inputTable) {
		this.inputTable = inputTable;
	}

}
