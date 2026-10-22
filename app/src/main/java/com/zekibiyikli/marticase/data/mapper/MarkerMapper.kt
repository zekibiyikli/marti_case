package com.zekibiyikli.marticase.data.mapper

import com.zekibiyikli.marticase.data.local.entity.MarkerEntity
import com.zekibiyikli.marticase.domain.model.LocationPoint

fun MarkerEntity.toDomain() = LocationPoint(latitude, longitude)

fun LocationPoint.toEntity() = MarkerEntity(latitude = latitude, longitude = longitude)
