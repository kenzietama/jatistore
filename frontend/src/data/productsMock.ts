export interface Product {
  id: string;
  name: string;
  price: number;
  originalPrice?: number;
  discountTag?: string;
  image: string;
  thumbnails: string[];
  soldBy: string;
  rating: number;
  reviewsCount: number;
  description: string;
  stock: number;
  category: string;
}

export const MOCK_PRODUCTS: Product[] = [
  {
    id: "bamboo-set",
    name: "Bamboo Utensil Set",
    price: 32.00,
    originalPrice: 45.00,
    discountTag: "Flash Sale",
    image: "https://lh3.googleusercontent.com/aida-public/AB6AXuC4Xoc3-Qr7EhgRpiCtr_eCRfLzOwO9nV-N8EZ_bU3AZM_naZRE4bOz2EYUjK4s58Bs-WMXZ1EjC8wvF_p0slCA1cgNS73IAoTH7xZPbDFkF3DsrD3ToTdKFLFH6ibE8VkGZSprZf9mR7ANaLdwOaC6OGzaOx7g0XwzqUBS6VXwQ81pxStU-hyQkATADHFhwDGTPcjGZeBqt_gUlzf1tC8u00Ycj4CH9W7BNqqHUQml0F1-Uv2YbGqGINZCBWSasK_U9FyR9Lbf10GK",
    thumbnails: [
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAbaUPH2gQyc-G3V8AxsdUc5YYDqzhDmwL3YzzxqSdI6Blnx2D8SLesy47f7C2dF7DFnjES-m8uLqzBXvQyMUG1pjNn10_Xowqwe49gFaXSgly4CUhiLUpL6sVdeMtOBSoOhBITMmyhf1eCNa2mBQxMSmwv0LKcIJckabvhluFZRvMSNpLM7Oy94OMQ8RUsKpPwAOgH1wPEKZH5HdafhfXJe9pr9aczVIQfZDKlYdx-lVqhy1crTo8-rbh7t8xeeN-6B3WWvb32BgXu",
      "https://lh3.googleusercontent.com/aida-public/AB6AXuChIOivRsJelLj3B5syp8MYgefUTpqRrN6Z5ZOk5eZx0VXxqI6zJzCIoPtHGNdUWDmVdm_vxhfVC7eAP_bnrmecSWAlYfY1r8xGfs9BuOF8vD0xdRkbjogPq4DuwCGRgQkYd0btRjaeO-krysWft0qn_zhYhvFBPUWiO4iL2CxDukhDzo4Bl2BQ74gCrdJOHLB_OUPnbxkJaWACuG_lQj3iD4d04Ijg4DqCBQo2WRV6XBkg_8QpYpJRZD6yzN4cCNakmebGfsDxuDNs",
      "https://lh3.googleusercontent.com/aida-public/AB6AXuBMOICIOrHfYDdjKPKL9XYxYG9F22CCjJHmWVo5kXUgUxpChP85VXumjviwzFdrWAuWxqEFu-iW6lR3L5BriTKJRAzpVOk56aorJxB41nkNVfnWqrLfQWFtWQ6oQGg6o7QeRpZY1V1dW79wG2hJVaGyRGS6BGHHUoMtybP5685fxY1O-VkgFkiPNtDdAeONCMo6E6WVunUqBA-UEk4YlXS0hclDGCMcok5_NfWjnLd2JOPqwQa05PZOXghxuoyHdUZZgby8tpznWf9p"
    ],
    soldBy: "Nature's Kitchen",
    rating: 5.0,
    reviewsCount: 128,
    description: "Elevate your everyday dining with our premium, sustainably sourced Bamboo Utensil Set. Lightweight, durable, and naturally antimicrobial. Perfect for home, office, or travel. Each set includes a fork, knife, and spoon.",
    stock: 14,
    category: "Rumah"
  },
];

export interface CartItem {
  id: string;
  name: string;
  store: string;
  price: number;
  originalPrice?: number;
  image: string;
  variant: string;
  quantity: number;
  maxStock: number;
}