package com.stacko.mall.application.service;

import com.stacko.mall.application.command.CreateShippingAddressCommand;
import com.stacko.mall.application.command.UpdateShippingAddressCommand;
import com.stacko.mall.domain.model.ShippingAddress;
import com.stacko.mall.domain.model.ShippingAddressId;
import com.stacko.mall.domain.repository.ShippingAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShippingAddressApplicationService {
    private final ShippingAddressRepository shippingAddressRepository;

    public ShippingAddressApplicationService(ShippingAddressRepository shippingAddressRepository) {
        this.shippingAddressRepository = shippingAddressRepository;
    }

    @Transactional
    public ShippingAddress create(CreateShippingAddressCommand command) {
        boolean shouldBeDefault = command.isDefaultAddress()
                || shippingAddressRepository.listByBuyer(command.getTenantId(), command.getBuyerId()).isEmpty();
        ShippingAddress address = ShippingAddress.create(
                command.getTenantId(),
                command.getBuyerId(),
                command.getReceiverName(),
                command.getReceiverPhone(),
                command.getProvince(),
                command.getCity(),
                command.getDistrict(),
                command.getDetailAddress(),
                shouldBeDefault
        );
        if (shouldBeDefault) {
            shippingAddressRepository.clearDefault(command.getTenantId(), command.getBuyerId(), address.getId());
        }
        return shippingAddressRepository.save(address);
    }

    @Transactional
    public ShippingAddress update(UpdateShippingAddressCommand command) {
        ShippingAddress address = get(command.getTenantId(), command.getBuyerId(), command.getAddressId());
        address.update(
                command.getReceiverName(),
                command.getReceiverPhone(),
                command.getProvince(),
                command.getCity(),
                command.getDistrict(),
                command.getDetailAddress(),
                command.isDefaultAddress()
        );
        if (command.isDefaultAddress()) {
            shippingAddressRepository.clearDefault(command.getTenantId(), command.getBuyerId(), address.getId());
        }
        return shippingAddressRepository.save(address);
    }

    @Transactional
    public ShippingAddress setDefault(String tenantId, String buyerId, String addressId) {
        ShippingAddress address = get(tenantId, buyerId, addressId);
        shippingAddressRepository.clearDefault(tenantId, buyerId, address.getId());
        address.markDefault();
        return shippingAddressRepository.save(address);
    }

    @Transactional
    public void delete(String tenantId, String buyerId, String addressId) {
        ShippingAddress address = get(tenantId, buyerId, addressId);
        shippingAddressRepository.delete(tenantId, buyerId, new ShippingAddressId(addressId));
        if (address.isDefaultAddress()) {
            shippingAddressRepository.listByBuyer(tenantId, buyerId).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.markDefault();
                        shippingAddressRepository.save(next);
                    });
        }
    }

    public ShippingAddress get(String tenantId, String buyerId, String addressId) {
        return shippingAddressRepository.findById(tenantId, buyerId, new ShippingAddressId(addressId))
                .orElseThrow(() -> new IllegalArgumentException("Shipping address not found"));
    }

    public List<ShippingAddress> list(String tenantId, String buyerId) {
        return shippingAddressRepository.listByBuyer(tenantId, buyerId);
    }
}
