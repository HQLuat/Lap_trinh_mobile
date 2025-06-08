package vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.repository;

import vn.edu.hcmuaf.fit.travelapp.admin.productManagement.data.model.Product;

public interface OnProductActionListener {
    void onDeleteProduct(Product product);
    void onUpdateProduct(Product product);
}
