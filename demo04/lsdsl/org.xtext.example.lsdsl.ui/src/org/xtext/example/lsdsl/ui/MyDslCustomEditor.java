package org.xtext.example.lsdsl.ui;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.xtext.ui.editor.XtextEditor;

public class MyDslCustomEditor extends XtextEditor {

    private PropertySheetPage propertySheetPage;

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == IPropertySheetPage.class) {
            if (propertySheetPage == null || propertySheetPage.getControl() == null || propertySheetPage.getControl().isDisposed()) {
                propertySheetPage = new PropertySheetPage();
            }
            return adapter.cast(propertySheetPage);
        }
        return super.getAdapter(adapter);
    }
}