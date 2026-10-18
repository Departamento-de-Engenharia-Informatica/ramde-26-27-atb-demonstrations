package org.xtext.example.lsdsl.ui;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.PropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;

public class MyEObjectNodeAdapterFactory implements IAdapterFactory {

    private AdapterFactoryContentProvider contentProvider;

    private AdapterFactoryContentProvider getContentProvider() {
        if (contentProvider == null) {
            ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
                ComposedAdapterFactory.Descriptor.Registry.INSTANCE
            );
            contentProvider = new AdapterFactoryContentProvider(adapterFactory);
        }
        return contentProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adapterType == IPropertySource.class && adaptableObject instanceof IOutlineNode) {
            if (adaptableObject instanceof EObjectNode) {
                EObjectNode node = (EObjectNode) adaptableObject;

                return node.readOnly(eObject -> {
                    if (eObject == null) {
                        return null;
                    }

                    IItemPropertySource itemPropertySource = (IItemPropertySource) getContentProvider()
                        .getAdapterFactory()
                        .adapt(eObject, IItemPropertySource.class);

                    if (itemPropertySource != null) {
                        return (T) new PropertySource(eObject, itemPropertySource) {

                            @Override
                            public Object getPropertyValue(Object id) {
                                Object value = super.getPropertyValue(id);
                                if (value instanceof ItemPropertyDescriptor.PropertyValueWrapper) {
                                    return ((ItemPropertyDescriptor.PropertyValueWrapper) value).getEditableValue(eObject);
                                }
                                return value;
                            }

                            @Override
                            public void setPropertyValue(Object id, Object value) {
                                IXtextDocument document = node.getDocument();
                                if (document != null) {
                                    // Execute modification inside a Write Transaction on the Xtext document
                                    document.modify(resource -> {
                                        Object valueToSet = value;
                                        if (valueToSet instanceof ItemPropertyDescriptor.PropertyValueWrapper) {
                                            valueToSet = ((ItemPropertyDescriptor.PropertyValueWrapper) valueToSet).getEditableValue(eObject);
                                        }
                                        super.setPropertyValue(id, valueToSet);
                                        return null;
                                    });
                                } else {
                                    super.setPropertyValue(id, value);
                                }
                            }
                        };
                    }
                    return null;
                });
            }
        }
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return new Class<?>[] { IPropertySource.class };
    }
}