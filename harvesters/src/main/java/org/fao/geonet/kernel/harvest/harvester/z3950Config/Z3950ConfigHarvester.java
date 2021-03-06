//=============================================================================
//=== Copyright (C) 2001-2011 Food and Agriculture Organization of the
//=== United Nations (FAO-UN), United Nations World Food Programme (WFP)
//=== and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.z3950Config;

import jeeves.exceptions.BadInputEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

import java.sql.SQLException;

//=============================================================================

public class Z3950ConfigHarvester extends AbstractHarvester
{
	public static final String TYPE = "z3950Config";

	//--------------------------------------------------------------------------
	//---
	//--- Static init
	//---
	//--------------------------------------------------------------------------

	public static void init(ServiceContext context) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Harvesting type
	//---
	//--------------------------------------------------------------------------

	public String getType() { return TYPE; }

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	protected void doInit(Element node) throws BadInputEx
	{
		params = new Z3950ConfigParams(dataMan);
        super.setParams(params);

        params.create(node);
	}


	//---------------------------------------------------------------------------
	//---
	//--- Add
	//---
	//---------------------------------------------------------------------------

	protected String doAdd(Dbms dbms, Element node) throws BadInputEx, SQLException
	{
		params = new Z3950ConfigParams(dataMan);
        super.setParams(params);
		//--- retrieve/initialize information
		params.create(node);

		String id = settingMan.add(dbms, "harvesting", "node", getType());

		storeNode(dbms, params, "id:"+id);

		return id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Update
	//---
	//---------------------------------------------------------------------------

	protected void doUpdate(Dbms dbms, String id, Element node) throws BadInputEx, SQLException
	{
		Z3950ConfigParams copy = params.copy();

		//--- update variables
		copy.update(node);

		String path = "harvesting/id:"+ id;

		settingMan.removeChildren(dbms, path);

		//--- update database
		storeNode(dbms, copy, path);

		params = copy;
        super.setParams(params);

    }

	//---------------------------------------------------------------------------

	protected void storeNodeExtra(Dbms dbms, AbstractParams p, String path,
											String siteId, String optionsId) throws SQLException
	{
		Z3950ConfigParams params = (Z3950ConfigParams) p;
        super.setParams(params);

        settingMan.add(dbms, "id:"+siteId, "host",    params.host);
		settingMan.add(dbms, "id:"+siteId, "port",    params.port);

		//--- store options

		settingMan.add(dbms, "id:"+optionsId, "clearConfig",  params.clearConfig);

		//--- store search nodes

		for (Search s : params.getSearches())
		{
			String  searchID = settingMan.add(dbms, path, "search", "");

			settingMan.add(dbms, "id:"+searchID, "freeText",   s.freeText);
			settingMan.add(dbms, "id:"+searchID, "title",      s.title);
			settingMan.add(dbms, "id:"+searchID, "abstract",   s.abstrac);
			settingMan.add(dbms, "id:"+searchID, "keywords",   s.keywords);
			settingMan.add(dbms, "id:"+searchID, "category",   s.category);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- addHarvestInfo
	//---
	//---------------------------------------------------------------------------

	public void addHarvestInfo(Element info, String id, String uuid)
	{
		super.addHarvestInfo(info, id, uuid);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Harvest
	//---
	//---------------------------------------------------------------------------

	protected void doHarvest(Logger log, ResourceManager rm) throws Exception
	{
		Dbms dbms = (Dbms) rm.open(Geonet.Res.MAIN_DB);

		Harvester h = new Harvester(log, context, dbms, params);
		result = h.harvest();
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Z3950ConfigParams params;
}