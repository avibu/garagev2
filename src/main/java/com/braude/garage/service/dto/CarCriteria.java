package com.braude.garage.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the Car entity. This class is used in CarResource to
 * receive all the possible filtering options from the Http GET request parameters.
 * For example the following could be a valid requests:
 * <code> /cars?id.greaterThan=5&amp;attr1.contains=something&amp;attr2.specified=false</code>
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CarCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter licensePlate;

    private StringFilter make;

    private StringFilter model;

    private IntegerFilter year;

    private LongFilter clientId;

    private LongFilter carServiceId;

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(StringFilter licensePlate) {
        this.licensePlate = licensePlate;
    }

    public StringFilter getMake() {
        return make;
    }

    public void setMake(StringFilter make) {
        this.make = make;
    }

    public StringFilter getModel() {
        return model;
    }

    public void setModel(StringFilter model) {
        this.model = model;
    }

    public IntegerFilter getYear() {
        return year;
    }

    public void setYear(IntegerFilter year) {
        this.year = year;
    }

    public LongFilter getClientId() {
        return clientId;
    }

    public void setClientId(LongFilter clientId) {
        this.clientId = clientId;
    }

    public LongFilter getCarServiceId() {
        return carServiceId;
    }

    public void setCarServiceId(LongFilter carServiceId) {
        this.carServiceId = carServiceId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CarCriteria that = (CarCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(licensePlate, that.licensePlate) &&
            Objects.equals(make, that.make) &&
            Objects.equals(model, that.model) &&
            Objects.equals(year, that.year) &&
            Objects.equals(clientId, that.clientId) &&
            Objects.equals(carServiceId, that.carServiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        licensePlate,
        make,
        model,
        year,
        clientId,
        carServiceId
        );
    }

    @Override
    public String toString() {
        return "CarCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (licensePlate != null ? "licensePlate=" + licensePlate + ", " : "") +
                (make != null ? "make=" + make + ", " : "") +
                (model != null ? "model=" + model + ", " : "") +
                (year != null ? "year=" + year + ", " : "") +
                (clientId != null ? "clientId=" + clientId + ", " : "") +
                (carServiceId != null ? "carServiceId=" + carServiceId + ", " : "") +
            "}";
    }

}
