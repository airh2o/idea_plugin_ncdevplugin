package com.air.nc5dev.ui.exportapifoxdoc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * "def7": {
 * "type": "string",
 * "title": "订单号"
 * },
 * "def8": {
 * "type": [
 * "string",
 * "null"
 * ],
 * "title": "订单号",
 * "description": "需要确定和上面的订单号是否一致内容。不用，不再传"
 * },
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FieldVO {
    String title;
    String description;
    Object type;

}