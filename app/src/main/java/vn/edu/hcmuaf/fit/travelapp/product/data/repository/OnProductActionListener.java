package vn.edu.hcmuaf.fit.travelapp.product.data.repository;

import vn.edu.hcmuaf.fit.travelapp.product.data.model.Product;

public interface OnProductActionListener {
    void onDeleteProduct(Product product);
    void onUpdateProduct(Product product);
}
