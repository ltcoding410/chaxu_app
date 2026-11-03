export interface Product {
  id: number;
  name: string;
  english: string;
  description: string;
  price: number;
  category: string;
  tone: string;
  tea: string;
  badge?: string;
  available: boolean;
}
export const categories = [
  "全部饮品",
  "人气推荐",
  "原叶鲜奶茶",
  "清爽鲜果茶",
  "轻盈纯茶",
];
export const products: Product[] = [
  {
    id: 1,
    name: "茉莉白月光",
    english: "Jasmine milk tea",
    description: "横县茉莉 · 清甜鲜奶 · 轻盈花香",
    price: 1800,
    category: "原叶鲜奶茶",
    tone: "jasmine",
    tea: "#d9bd8f",
    badge: "人气 NO.1",
    available: true,
  },
  {
    id: 2,
    name: "山野栀子青",
    english: "Gardenia milk tea",
    description: "栀子绿茶 · 牧场鲜奶 · 一口山野",
    price: 1800,
    category: "原叶鲜奶茶",
    tone: "gardenia",
    tea: "#c7cd9e",
    badge: "当季新品",
    available: true,
  },
  {
    id: 3,
    name: "蜜桃乌龙",
    english: "Peach oolong tea",
    description: "白桃果肉 · 高山乌龙 · 鲜甜回甘",
    price: 2200,
    category: "清爽鲜果茶",
    tone: "peach",
    tea: "#e6ab83",
    available: true,
  },
  {
    id: 4,
    name: "桂花慢乌龙",
    english: "Osmanthus oolong latte",
    description: "金桂花香 · 炭焙乌龙 · 醇厚奶香",
    price: 2000,
    category: "原叶鲜奶茶",
    tone: "osmanthus",
    tea: "#ba9265",
    available: true,
  },
  {
    id: 5,
    name: "青提茉莉",
    english: "Grape jasmine tea",
    description: "鲜剥青提 · 茉莉茶底 · 清新有层次",
    price: 2400,
    category: "清爽鲜果茶",
    tone: "grape",
    tea: "#b9c585",
    badge: "清爽之选",
    available: true,
  },
  {
    id: 6,
    name: "云雾知春",
    english: "Spring green tea",
    description: "高山原叶 · 现萃清茶 · 自然回甘",
    price: 1200,
    category: "轻盈纯茶",
    tone: "spring",
    tea: "#c7b678",
    available: true,
  },
];
export const templates = [
  {
    id: 1,
    name: "日常小确幸",
    cost: 100,
    discount: 500,
    minimum: 2000,
    description: "满 20 元可用",
  },
  {
    id: 2,
    name: "好茶与你分享",
    cost: 180,
    discount: 1000,
    minimum: 3500,
    description: "满 35 元可用",
  },
];
export const money = (cents: number) =>
  new Intl.NumberFormat("zh-CN", {
    style: "currency",
    currency: "CNY",
    maximumFractionDigits: 2,
  }).format(cents / 100);
