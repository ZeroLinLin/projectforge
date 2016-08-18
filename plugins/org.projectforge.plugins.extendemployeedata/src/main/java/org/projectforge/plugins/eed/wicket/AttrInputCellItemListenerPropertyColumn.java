/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.eed.wicket;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.projectforge.business.fibu.EmployeeDO;
import org.projectforge.business.fibu.EmployeeTimedDO;
import org.projectforge.business.fibu.api.EmployeeService;
import org.projectforge.framework.persistence.attr.impl.GuiAttrSchemaService;
import org.projectforge.web.wicket.CellItemListener;

import de.micromata.genome.db.jpa.tabattr.api.AttrDescription;
import de.micromata.genome.db.jpa.tabattr.api.AttrGroup;
import de.micromata.genome.db.jpa.tabattr.api.TimeableService;

/**
 * Supports CellItemListener.
 * 
 * @author Florian Blumenstein
 * 
 */
public class AttrInputCellItemListenerPropertyColumn<T> extends PropertyColumn<T, String>
{
  private static final long serialVersionUID = -5876306905464081437L;

  protected CellItemListener<T> cellItemListener;

  private TimeableService<Integer, EmployeeTimedDO> timeableService;

  private GuiAttrSchemaService guiAttrSchemaService;

  private EmployeeService employeeService;

  private String groupAttribute;

  private Integer selectedMonth;

  private Integer selectedYear;

  /**
   * @param displayModelString For creation of new Model<String>.
   * @param sortProperty
   * @param propertyExpression
   * @param cellItemListener
   */
  public AttrInputCellItemListenerPropertyColumn(final IModel<String> displayModel, final String sortProperty,
      final String propertyExpression, final String groupAttribute,
      final CellItemListener<T> cellItemListener, TimeableService<Integer, EmployeeTimedDO> timeableService,
      EmployeeService employeeService, GuiAttrSchemaService guiAttrSchemaService, Integer selectedMonth,
      Integer selectedYear)
  {
    super(displayModel, sortProperty, propertyExpression);
    this.cellItemListener = cellItemListener;
    this.groupAttribute = groupAttribute;
    this.timeableService = timeableService;
    this.employeeService = employeeService;
    this.guiAttrSchemaService = guiAttrSchemaService;
    this.selectedMonth = selectedMonth;
    this.selectedYear = selectedYear;
  }

  /**
   * Override this method if you want to have tool-tips.
   * 
   * @return
   */
  public String getTooltip(final T object)
  {
    return null;
  }

  /**
   * Call CellItemListener. If a property model object is of type I18nEnum then the translation is automatically used.
   * 
   * @see org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn#populateItem(org.apache.wicket.markup.repeater.Item,
   *      java.lang.String, org.apache.wicket.model.IModel)
   * @see CellItemListener#populateItem(Item, String, IModel)
   */
  @Override
  public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel)
  {
    final EmployeeDO employee = (EmployeeDO) rowModel.getObject();
    Calendar cal = new GregorianCalendar(selectedYear, selectedMonth - 1, 1, 0, 0);
    EmployeeTimedDO row = timeableService.getAttrRowForSameMonth(employee, getPropertyExpression(), cal.getTime());
    if (row == null) {
      row = employeeService.addNewTimeAttributeRow(employee, getPropertyExpression());
      row.setStartTime(cal.getTime());
    }
    AttrGroup attrGroup = guiAttrSchemaService.getAttrGroup(employee, getPropertyExpression());
    AttrDescription attrDescription = attrGroup.getDescriptions().stream()
        .filter(attrDesc -> attrDesc.getPropertyName().equals(groupAttribute)).findFirst().orElse(null);
    item.add((Component) guiAttrSchemaService.createWicketComponent(componentId, attrGroup, attrDescription, row));
    if (cellItemListener != null) {
      cellItemListener.populateItem(item, componentId, rowModel);
    }
  }
}
